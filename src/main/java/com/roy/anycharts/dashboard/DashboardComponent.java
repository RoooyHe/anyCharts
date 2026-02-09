package com.roy.anycharts.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardComponent {
  private String id;
  private String type; // chart, text, image
  private double x;
  private double y;
  private int width;
  private int height;
  private String chartId; // 关联的图表 ID
  private String title;
}
