package com.roy.anycharts.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
  private String id;
  private String name;
  private int width;
  private int height;
  private LocalDateTime createdAt;
  private List<DashboardComponent> components = new ArrayList<>();
  
  public Dashboard(String id, String name, int width, int height, List<DashboardComponent> components) {
    this.id = id;
    this.name = name;
    this.width = width;
    this.height = height;
    this.createdAt = LocalDateTime.now();
    this.components = components;
  }
}
