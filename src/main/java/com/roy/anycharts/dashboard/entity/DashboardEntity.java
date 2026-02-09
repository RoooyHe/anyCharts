package com.roy.anycharts.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dashboards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEntity {
  @Id
  private String id;
  
  private String name;
  
  private int width;
  
  private int height;
  
  private LocalDateTime createdAt;
  
  @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<DashboardComponentEntity> components = new ArrayList<>();
}
