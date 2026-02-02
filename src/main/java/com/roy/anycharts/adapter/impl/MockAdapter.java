package com.roy.anycharts.adapter.impl;

import com.roy.anycharts.adapter.DataSourceAdapter;
import java.time.Duration;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class MockAdapter implements DataSourceAdapter {
  private final ObjectMapper mapper = new ObjectMapper();

  private static final String SALES_DATA =
      """
      {"items": [
        {"date": "周一", "value": 120},
        {"date": "周二", "value": 132},
        {"date": "周三", "value": 101},
        {"date": "周四", "value": 134},
        {"date": "周五", "value": 90},
        {"date": "周六", "value": 230},
        {"date": "周日", "value": 210}
      ]}
      """;

  private static final String TREND_DATA =
      """
      {"items": [
        {"date": "1月", "value": 10},
        {"date": "2月", "value": 15},
        {"date": "3月", "value": 23},
        {"date": "4月", "value": 35},
        {"date": "5月", "value": 42},
        {"date": "6月", "value": 58}
      ]}
      """;

  private static final String DISTRIBUTION_DATA =
      """
      {"items": [
        {"name": "苹果", "value": 25},
        {"name": "香蕉", "value": 35},
        {"name": "橙子", "value": 20},
        {"name": "葡萄", "value": 15},
        {"name": "西瓜", "value": 5}
      ]}
      """;

  private static final String CORRELATION_DATA =
      """
      {"items": [
        [10.0, 8.04],
        [8.07, 6.95],
        [13.0, 7.58],
        [9.05, 8.81],
        [11.0, 8.33],
        [14.0, 9.96],
        [6.0, 7.24],
        [4.0, 4.26],
        [12.0, 10.84],
        [7.0, 4.82],
        [5.0, 5.68]
      ]}
      """;

  private static final String GROWTH_DATA =
      """
      {"items": [
        {"date": "1月", "value": 100},
        {"date": "2月", "value": 120},
        {"date": "3月", "value": 150},
        {"date": "4月", "value": 180},
        {"date": "5月", "value": 220},
        {"date": "6月", "value": 280}
      ]}
      """;

  @Override
  public Mono<JsonNode> execute(String query, Map<String, Object> params) {
    try {
      String json =
          switch (query) {
            case "mock:trend" -> TREND_DATA;
            case "mock:distribution" -> DISTRIBUTION_DATA;
            case "mock:correlation" -> CORRELATION_DATA;
            case "mock:growth" -> GROWTH_DATA;
            default -> SALES_DATA;
          };
      return Mono.just(mapper.readTree(json));
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

  @Override
  public Flux<JsonNode> subscribe(String query, Map<String, Object> params) {
    // 每2秒发一条增量，用于 demo subscription
    return Flux.interval(Duration.ofSeconds(2))
        .handle(
            (i, sink) -> {
              try {
                String json =
                    String.format(
                        "{\"items\":[{\"date\":\"2026-01-%02d\",\"value\":%d}]}",
                        3 + i.intValue(), 100 + i.intValue() * 10);
                sink.next(mapper.readTree(json));
              } catch (Exception e) {
                sink.error(new RuntimeException(e));
              }
            });
  }

  @Override
  public String id() {
    return "mock-adapter";
  }
}
