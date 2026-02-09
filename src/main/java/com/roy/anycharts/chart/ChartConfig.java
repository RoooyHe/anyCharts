package com.roy.anycharts.chart;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChartConfig {
  private String id;
  private String title;
  private String chartType; // bar, line, pie, scatter, area, etc.
  private JsonNode optionTemplate;
  private LocalDateTime createdAt;
  private List<DataSourceBinding> bindings = new ArrayList<>();

  public ChartConfig(String id, String title, String chartType, JsonNode optionTemplate) {
    this.id = id;
    this.title = title;
    this.chartType = chartType;
    this.optionTemplate = optionTemplate;
    this.createdAt = LocalDateTime.now();
  }
}
