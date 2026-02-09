package com.roy.anycharts.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roy.anycharts.chart.ChartConfig;
import com.roy.anycharts.chart.ChartConfigStore;
import com.roy.anycharts.chart.ChartService;
import com.roy.anycharts.chart.DataSourceBinding;
import com.roy.anycharts.dashboard.Dashboard;
import com.roy.anycharts.dashboard.DashboardComponent;
import com.roy.anycharts.dashboard.DashboardStore;
import com.roy.anycharts.datasource.DatabaseConnection;
import com.roy.anycharts.datasource.DatabaseConnectionStore;
import com.roy.anycharts.datasource.DatabaseMetadataService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ChartController {
  private final ChartService chartService;
  private final ChartConfigStore chartConfigStore;
  private final DatabaseConnectionStore databaseConnectionStore;
  private final DatabaseMetadataService databaseMetadataService;
  private final DashboardStore dashboardStore;
  private final ObjectMapper mapper = new ObjectMapper();

  @QueryMapping
  public Mono<Map<String, Object>> renderChart(
      @Argument String id, @Argument Map<String, Object> variables) {
    return chartService.renderChart(id, variables).map(json -> Map.of("id", id, "option", json));
  }

  @QueryMapping
  public Map<String, Object> chartConfig(@Argument String id) {
    return chartConfigStore.get(id).map(this::toChartConfigDto).orElse(null);
  }

  @QueryMapping
  public List<Map<String, Object>> allCharts() {
    try {
      System.out.println("=== 收到 allCharts 查询请求 ===");
      Map<String, ChartConfig> all = chartConfigStore.getAll();
      System.out.println("从数据库获取到 " + all.size() + " 个图表配置");
      
      List<Map<String, Object>> result = all.values().stream()
          .map(this::toChartConfigDto)
          .collect(Collectors.toList());
      
      System.out.println("返回 " + result.size() + " 个图表");
      return result;
    } catch (Exception e) {
      System.err.println("❌ allCharts 查询失败: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to fetch charts: " + e.getMessage(), e);
    }
  }

  private Map<String, Object> toChartConfigDto(ChartConfig cfg) {
    return Map.of(
        "id", cfg.getId(),
        "title", cfg.getTitle(),
        "chartType", cfg.getChartType() != null ? cfg.getChartType() : "",
        "optionTemplate", cfg.getOptionTemplate(),
        "bindings", cfg.getBindings());
  }

  @MutationMapping
  public Map<String, Object> saveChartConfig(@Argument Map<String, Object> input) {
    String id = (String) input.getOrDefault("id", "chart-" + System.currentTimeMillis());
    String title = (String) input.get("title");
    String chartType = (String) input.getOrDefault("chartType", "bar");
    JsonNode optionTemplate = mapper.valueToTree(input.get("optionTemplate"));
    
    ChartConfig config = new ChartConfig(id, title, chartType, optionTemplate);
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> bindingsInput = (List<Map<String, Object>>) input.get("bindings");
    if (bindingsInput != null) {
      for (Map<String, Object> b : bindingsInput) {
        DataSourceBinding binding = new DataSourceBinding(
            (String) b.get("name"),
            (String) b.getOrDefault("datasourceId", "mock-adapter"),
            (String) b.getOrDefault("query", "mock:sales"),
            (String) b.getOrDefault("mappingPath", ""),
            (String) b.get("bindingKey"),
            Boolean.TRUE.equals(b.get("stream"))
        );
        config.getBindings().add(binding);
      }
    }
    
    chartConfigStore.save(config);
    return toChartConfigDto(config);
  }

  @MutationMapping
  public boolean deleteChartConfig(@Argument String id) {
    return chartConfigStore.delete(id);
  }

  @SubscriptionMapping
  public Flux<Map<String, Object>> chartUpdates(
      @Argument String id, @Argument Map<String, Object> variables) {
    return chartService.subscribeChart(id, variables).map(json -> Map.of("id", id, "option", json));
  }

  // ========== 数据库连接管理 ==========
  
  @QueryMapping
  public List<DatabaseConnection> databaseConnections() {
    return databaseConnectionStore.getAll();
  }

  @QueryMapping
  public DatabaseConnection databaseConnection(@Argument String id) {
    return databaseConnectionStore.get(id).orElse(null);
  }

  @QueryMapping
  public Mono<List<String>> databaseTables(@Argument String connectionId) {
    return databaseMetadataService.getTables(connectionId);
  }

  @QueryMapping
  public Mono<List<Map<String, Object>>> databaseColumns(
      @Argument String connectionId, @Argument String tableName) {
    return databaseMetadataService.getColumns(connectionId, tableName);
  }

  @QueryMapping
  public Mono<List<Map<String, Object>>> previewTableData(
      @Argument String connectionId, @Argument String tableName, @Argument Integer limit) {
    return databaseMetadataService.previewData(connectionId, tableName, limit != null ? limit : 10);
  }

  @MutationMapping
  public DatabaseConnection saveDatabaseConnection(@Argument Map<String, Object> input) {
    String id = (String) input.getOrDefault("id", "db-" + System.currentTimeMillis());
    DatabaseConnection connection = new DatabaseConnection(
        id,
        (String) input.get("name"),
        (String) input.get("jdbcUrl"),
        (String) input.get("username"),
        (String) input.get("password"),
        (String) input.get("driverClass"),
        Boolean.TRUE.equals(input.get("active"))
    );
    databaseConnectionStore.save(connection);
    return connection;
  }

  @MutationMapping
  public boolean deleteDatabaseConnection(@Argument String id) {
    return databaseConnectionStore.delete(id);
  }

  // ========== 大屏管理 ==========
  
  @QueryMapping
  public Dashboard dashboard(@Argument String id) {
    return dashboardStore.get(id).orElse(null);
  }

  @QueryMapping
  public List<Dashboard> allDashboards() {
    return dashboardStore.getAll();
  }

  @MutationMapping
  public Dashboard saveDashboard(@Argument Map<String, Object> input) {
    String id = (String) input.getOrDefault("id", "dashboard-" + System.currentTimeMillis());
    String name = (String) input.get("name");
    int width = (Integer) input.getOrDefault("width", 1920);
    int height = (Integer) input.getOrDefault("height", 1080);
    
    Dashboard dashboard = new Dashboard(id, name, width, height, new java.util.ArrayList<>());
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> componentsInput = (List<Map<String, Object>>) input.get("components");
    if (componentsInput != null) {
      for (Map<String, Object> c : componentsInput) {
        DashboardComponent component = new DashboardComponent(
            (String) c.get("id"),
            (String) c.get("type"),
            (Double) c.get("x"),
            (Double) c.get("y"),
            (Integer) c.get("width"),
            (Integer) c.get("height"),
            (String) c.get("chartId"),
            (String) c.get("title")
        );
        dashboard.getComponents().add(component);
      }
    }
    
    dashboardStore.save(dashboard);
    return dashboard;
  }

  @MutationMapping
  public boolean deleteDashboard(@Argument String id) {
    return dashboardStore.delete(id);
  }
}
