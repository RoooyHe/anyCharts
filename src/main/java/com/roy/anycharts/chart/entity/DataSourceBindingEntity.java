package com.roy.anycharts.chart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "data_source_binding")
public class DataSourceBindingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String datasourceId;
  private String query;
  private String mappingPath;
  private String bindingKey;
  private Boolean stream;

  @ManyToOne
  @JoinColumn(name = "chart_config_id")
  private ChartConfigEntity chartConfig;

  public DataSourceBindingEntity() {}

  public DataSourceBindingEntity(
      String name,
      String datasourceId,
      String query,
      String mappingPath,
      String bindingKey,
      Boolean stream) {
    this.name = name;
    this.datasourceId = datasourceId;
    this.query = query;
    this.mappingPath = mappingPath;
    this.bindingKey = bindingKey;
    this.stream = stream;
  }

}
