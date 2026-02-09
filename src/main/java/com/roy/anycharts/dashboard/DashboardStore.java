package com.roy.anycharts.dashboard;

import com.roy.anycharts.dashboard.entity.DashboardComponentEntity;
import com.roy.anycharts.dashboard.entity.DashboardEntity;
import com.roy.anycharts.dashboard.repository.DashboardRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DashboardStore {
  private final DashboardRepository repository;

  @Transactional(readOnly = true)
  public Optional<Dashboard> get(String id) {
    return repository.findById(id).map(this::toDashboard);
  }

  @Transactional
  public void save(Dashboard dashboard) {
    DashboardEntity entity = toEntity(dashboard);
    repository.save(entity);
  }

  @Transactional
  public boolean delete(String id) {
    if (repository.existsById(id)) {
      repository.deleteById(id);
      return true;
    }
    return false;
  }

  @Transactional(readOnly = true)
  public List<Dashboard> getAll() {
    return repository.findAll().stream()
        .map(this::toDashboard)
        .collect(Collectors.toList());
  }

  private Dashboard toDashboard(DashboardEntity entity) {
    Dashboard dashboard = new Dashboard();
    dashboard.setId(entity.getId());
    dashboard.setName(entity.getName());
    dashboard.setWidth(entity.getWidth());
    dashboard.setHeight(entity.getHeight());
    dashboard.setCreatedAt(entity.getCreatedAt());
    
    List<DashboardComponent> components = entity.getComponents().stream()
        .map(c -> new DashboardComponent(
            c.getComponentId(),
            c.getType(),
            c.getX(),
            c.getY(),
            c.getWidth(),
            c.getHeight(),
            c.getChartId(),
            c.getTitle()
        ))
        .collect(Collectors.toList());
    
    dashboard.setComponents(components);
    return dashboard;
  }

  private DashboardEntity toEntity(Dashboard dashboard) {
    DashboardEntity entity = new DashboardEntity();
    entity.setId(dashboard.getId());
    entity.setName(dashboard.getName());
    entity.setWidth(dashboard.getWidth());
    entity.setHeight(dashboard.getHeight());
    entity.setCreatedAt(dashboard.getCreatedAt() != null ? dashboard.getCreatedAt() : java.time.LocalDateTime.now());
    
    List<DashboardComponentEntity> components = dashboard.getComponents().stream()
        .map(c -> {
          DashboardComponentEntity comp = new DashboardComponentEntity();
          comp.setComponentId(c.getId());
          comp.setType(c.getType());
          comp.setX(c.getX());
          comp.setY(c.getY());
          comp.setWidth(c.getWidth());
          comp.setHeight(c.getHeight());
          comp.setChartId(c.getChartId());
          comp.setTitle(c.getTitle());
          comp.setDashboard(entity);
          return comp;
        })
        .collect(Collectors.toList());
    
    entity.setComponents(components);
    return entity;
  }
}
