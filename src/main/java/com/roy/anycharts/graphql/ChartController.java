package com.roy.anycharts.graphql;

import com.roy.anycharts.chart.ChartConfig;
import com.roy.anycharts.chart.ChartConfigStore;
import com.roy.anycharts.chart.ChartService;
import com.roy.anycharts.chart.DataSourceBinding;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Controller
public class ChartController {
  private final ChartService chartService;
  private final ChartConfigStore chartConfigStore;
  private final ObjectMapper mapper = new ObjectMapper();

  public ChartController(ChartService chartService, ChartConfigStore chartConfigStore) {
    this.chartService = chartService;
    this.chartConfigStore = chartConfigStore;
  }

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
    return chartConfigStore.getAll().values().stream()
        .map(this::toChartConfigDto)
        .collect(Collectors.toList());
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
}
