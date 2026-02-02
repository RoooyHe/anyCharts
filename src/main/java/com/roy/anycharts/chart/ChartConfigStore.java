package com.roy.anycharts.chart;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class ChartConfigStore {
  private final Map<String, ChartConfig> store = new HashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();

  public ChartConfigStore() throws Exception {
    // demo: template 有 "{{binding:...}}" 占位符（字符串形式），后续会替换
    String template =
        "{ \"title\": {\"text\":\"销售\"}, \"xAxis\": {\"data\": \"{{binding:categories}}\"}, \"series\": [{\"type\":\"bar\",\"data\": \"{{binding:series1}}\" }] }";
    JsonNode templateNode = mapper.readTree(template);
    ChartConfig cfg = new ChartConfig("sales-2026", "销售图", templateNode);
    DataSourceBinding b =
        new DataSourceBinding(
            "ds1", "mock-adapter", "mock:sales", "$.items[*].date", "categories", false);
    DataSourceBinding b2 =
        new DataSourceBinding(
            "ds2", "mock-adapter", "mock:sales", "$.items[*].value", "series1", false);
    cfg.getBindings().add(b);
    cfg.getBindings().add(b2);
    store.put(cfg.getId(), cfg);
  }

  public Optional<ChartConfig> get(String id) {
    return Optional.ofNullable(store.get(id));
  }
}
