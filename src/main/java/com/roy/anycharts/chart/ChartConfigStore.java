package com.roy.anycharts.chart;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public class ChartConfigStore {
  private final Map<String, ChartConfig> store = new HashMap<>();

  public ChartConfigStore() {
    // ===== 柱状图 (Bar) =====
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ObjectNode barTemplate = factory.objectNode();
    barTemplate.set("title", factory.objectNode().put("text", "销售数据"));
    barTemplate.set("tooltip", factory.objectNode().put("trigger", "axis"));
    barTemplate.set("legend", factory.objectNode().putArray("data").add("销量"));
    ObjectNode xAxis = factory.objectNode().put("type", "category");
    xAxis.put("data", "{{binding:categories}}");
    barTemplate.set("xAxis", xAxis);
    barTemplate.set("yAxis", factory.objectNode().put("type", "value"));
    ObjectNode series = factory.objectNode();
    series.put("name", "销量");
    series.put("type", "bar");
    series.put("data", "{{binding:series1}}");
    barTemplate.set("series", factory.arrayNode().add(series));
    ChartConfig barChart = new ChartConfig("sales-bar", "销售柱状图", "bar", barTemplate);
    barChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "categories",
                "mock-adapter",
                "mock:sales",
                "$.items[*].date",
                "categories",
                false));
    barChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "series1", "mock-adapter", "mock:sales", "$.items[*].value", "series1", false));
    store.put(barChart.getId(), barChart);

    // ===== 折线图 (Line) =====
    ObjectNode lineTemplate = factory.objectNode();
    lineTemplate.set("title", factory.objectNode().put("text", "趋势分析"));
    lineTemplate.set("tooltip", factory.objectNode().put("trigger", "axis"));
    lineTemplate.set("legend", factory.objectNode().putArray("data").add("增长率"));
    ObjectNode lxAxis = factory.objectNode().put("type", "category");
    lxAxis.put("boundaryGap", false);
    lxAxis.put("data", "{{binding:categories}}");
    lineTemplate.set("xAxis", lxAxis);
    lineTemplate.set("yAxis", factory.objectNode().put("type", "value"));
    ObjectNode lSeries = factory.objectNode();
    lSeries.put("name", "增长率");
    lSeries.put("type", "line");
    lSeries.put("data", "{{binding:series1}}");
    lSeries.put("smooth", true);
    lineTemplate.set("series", factory.arrayNode().add(lSeries));
    ChartConfig lineChart = new ChartConfig("trend-line", "趋势折线图", "line", lineTemplate);
    lineChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "categories",
                "mock-adapter",
                "mock:trend",
                "$.items[*].date",
                "categories",
                false));
    lineChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "series1", "mock-adapter", "mock:trend", "$.items[*].value", "series1", false));
    store.put(lineChart.getId(), lineChart);

    // ===== 饼图 (Pie) - 使用占位符 =====
    String pieJson =
        "{\"title\":{\"text\":\"占比分布\",\"left\":\"center\"},\"tooltip\":{\"trigger\":\"item\"},\"legend\":{\"orient\":\"vertical\",\"left\":\"left\"},\"series\":[{\"type\":\"pie\",\"radius\":\"50%\",\"data\":\"{{binding:pieData}}\"}]}";
    ObjectMapper mapper = new ObjectMapper();
    ChartConfig pieChart =
        new ChartConfig("distribution-pie", "分布饼图", "pie", mapper.readTree(pieJson));
    pieChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "pieData", "mock-adapter", "mock:distribution", "$.items[*]", "pieData", false));
    store.put(pieChart.getId(), pieChart);

    // ===== 散点图 (Scatter) - 使用占位符 =====
    String scatterJson =
        "{\"title\":{\"text\":\"相关性分析\"},\"tooltip\":{},\"xAxis\":{\"name\":\"X\",\"type\":\"value\"},\"yAxis\":{\"name\":\"Y\",\"type\":\"value\"},\"series\":[{\"type\":\"scatter\",\"symbolSize\":20,\"data\":\"{{binding:scatterData}}\"}]}";
    ChartConfig scatterChart =
        new ChartConfig("correlation-scatter", "散点图", "scatter", mapper.readTree(scatterJson));
    scatterChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "scatterData",
                "mock-adapter",
                "mock:correlation",
                "$.items[*]",
                "scatterData",
                false));
    store.put(scatterChart.getId(), scatterChart);

    // ===== 面积图 (Area) =====
    ObjectNode areaTemplate = factory.objectNode();
    areaTemplate.set("title", factory.objectNode().put("text", "累计增长"));
    areaTemplate.set("tooltip", factory.objectNode().put("trigger", "axis"));
    ObjectNode axAxis = factory.objectNode().put("type", "category");
    axAxis.put("boundaryGap", false);
    axAxis.put("data", "{{binding:categories}}");
    areaTemplate.set("xAxis", axAxis);
    areaTemplate.set("yAxis", factory.objectNode().put("type", "value"));
    ObjectNode aSeries = factory.objectNode();
    aSeries.put("name", "累计");
    aSeries.put("type", "line");
    aSeries.set("areaStyle", factory.objectNode());
    aSeries.put("data", "{{binding:series1}}");
    areaTemplate.set("series", factory.arrayNode().add(aSeries));
    ChartConfig areaChart = new ChartConfig("growth-area", "面积图", "area", areaTemplate);
    areaChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "categories",
                "mock-adapter",
                "mock:growth",
                "$.items[*].date",
                "categories",
                false));
    areaChart
        .getBindings()
        .add(
            new DataSourceBinding(
                "series1", "mock-adapter", "mock:growth", "$.items[*].value", "series1", false));
    store.put(areaChart.getId(), areaChart);

    // ===== 保留原有示例 (兼容) =====
    String oldJson =
        "{\"title\":{\"text\":\"销售\"},\"xAxis\":{\"data\":\"{{binding:categories}}\"},\"yAxis\":{},\"series\":[{\"type\":\"bar\",\"data\":\"{{binding:series1}}\"}]}";
    ChartConfig cfg = new ChartConfig("sales-2026", "销售图", "bar", mapper.readTree(oldJson));
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

  public void save(ChartConfig config) {
    store.put(config.getId(), config);
  }

  public boolean delete(String id) {
    return store.remove(id) != null;
  }

  public Map<String, ChartConfig> getAll() {
    return new HashMap<>(store);
  }
}
