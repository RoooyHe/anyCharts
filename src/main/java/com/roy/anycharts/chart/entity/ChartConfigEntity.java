package com.roy.anycharts.chart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "chart_config")
public class ChartConfigEntity {
  @Id private String id;

  private String title;

  private String chartType;

  @Column(columnDefinition = "TEXT")
  private String optionTemplate;

  @OneToMany(mappedBy = "chartConfig", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DataSourceBindingEntity> bindings = new ArrayList<>();

  public ChartConfigEntity() {}

  public ChartConfigEntity(String id, String title, String chartType, String optionTemplate) {
    this.id = id;
    this.title = title;
    this.chartType = chartType;
    this.optionTemplate = optionTemplate;
  }

}
