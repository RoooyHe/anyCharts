package com.roy.anycharts.chart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceBinding {
  private String name;
  private String datasourceId;
  private String query;
  private String mappingPath;
  private String bindingKey;
  private boolean stream;
}
