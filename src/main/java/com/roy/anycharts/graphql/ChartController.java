package com.roy.anycharts.graphql;

import com.roy.anycharts.chart.ChartConfig;
import com.roy.anycharts.chart.ChartConfigStore;
import com.roy.anycharts.chart.ChartService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ChartController {
  private final ChartService chartService;
  private final ChartConfigStore chartConfigStore;

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

  @SubscriptionMapping
  public Flux<Map<String, Object>> chartUpdates(
      @Argument String id, @Argument Map<String, Object> variables) {
    return chartService.subscribeChart(id, variables).map(json -> Map.of("id", id, "option", json));
  }
}
