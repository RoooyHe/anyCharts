package com.roy.anycharts.chart;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

public class ChartConfig {
  private String id;
  private String title;
  private String chartType; // bar, line, pie, scatter, area, etc.
  private JsonNode optionTemplate;
  private List<DataSourceBinding> bindings = new ArrayList<>();

  public ChartConfig() {}

  public ChartConfig(String id, String title, String chartType, JsonNode optionTemplate) {
    this.id = id;
    this.title = title;
    this.chartType = chartType;
    this.optionTemplate = optionTemplate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getChartType() {
    return chartType;
  }

  public void setChartType(String chartType) {
    this.chartType = chartType;
  }

  public JsonNode getOptionTemplate() {
    return optionTemplate;
  }

  public void setOptionTemplate(JsonNode optionTemplate) {
    this.optionTemplate = optionTemplate;
  }

  public List<DataSourceBinding> getBindings() {
    return bindings;
  }

  public void setBindings(List<DataSourceBinding> bindings) {
    this.bindings = bindings;
  }
}
