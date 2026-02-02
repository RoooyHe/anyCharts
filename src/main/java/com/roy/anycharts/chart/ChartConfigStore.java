package com.roy.anycharts.chart;


import tools.jackson.databind.ObjectMapper;

import java.util.*;

public class ChartConfigStore {
    private final Map<String, ChartConfig> store = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ChartConfigStore() throws Exception {
        // demo: 一个简单模板，绑定到 mock-adapter
        String template = "{ \"title\": {\"text\":\"销售\"}, \"xAxis\": {\"data\": {{binding:categories}}}, \"series\": [{\"type\":\"bar\",\"data\": {{binding:series1}} }] }";
        ChartConfig cfg = new ChartConfig("sales-2026", "销售图", mapper.readTree(template));
        cfg.getBindings().add(new DataSourceBinding("ds1", "mock-adapter", "mock:sales", mapper.createObjectNode(), false, "categories", "$.items[*].date", "series1", "$.items[*].value"));
        store.put(cfg.getId(), cfg);
    }

    public Optional<ChartConfig> get(String id) { return Optional.ofNullable(store.get(id)); }
}