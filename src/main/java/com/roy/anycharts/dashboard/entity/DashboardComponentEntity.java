package com.roy.anycharts.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dashboard_components")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardComponentEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private String componentId;
  
  private String type;
  
  private double x;
  
  private double y;
  
  private int width;
  
  private int height;
  
  private String chartId;
  
  private String title;
  
  @ManyToOne
  @JoinColumn(name = "dashboard_id")
  private DashboardEntity dashboard;
}
