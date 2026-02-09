package com.roy.anycharts.chart;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roy.anycharts.chart.entity.ChartConfigEntity;
import com.roy.anycharts.chart.entity.DataSourceBindingEntity;
import com.roy.anycharts.chart.repository.ChartConfigRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChartConfigStore {
  private final ChartConfigRepository repository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ChartConfigStore(ChartConfigRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Optional<ChartConfig> get(String id) {
    return repository.findById(id).map(this::toChartConfig);
  }

  @Transactional
  public void save(ChartConfig config) {
    ChartConfigEntity entity = toEntity(config);
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
  public Map<String, ChartConfig> getAll() {
    Map<String, ChartConfig> result = new HashMap<>();
    repository.findAll().forEach(entity -> result.put(entity.getId(), toChartConfig(entity)));
    return result;
  }

  private ChartConfig toChartConfig(ChartConfigEntity entity) {
    try {
      JsonNode optionTemplate = objectMapper.readTree(entity.getOptionTemplate());
      ChartConfig config =
          new ChartConfig(entity.getId(), entity.getTitle(), entity.getChartType(), optionTemplate);
      config.setCreatedAt(entity.getCreatedAt());
      entity
          .getBindings()
          .forEach(
              b ->
                  config
                      .getBindings()
                      .add(
                          new DataSourceBinding(
                              b.getName(),
                              b.getDatasourceId(),
                              b.getQuery(),
                              b.getMappingPath(),
                              b.getBindingKey(),
                              b.getStream())));
      return config;
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse chart config", e);
    }
  }

  private ChartConfigEntity toEntity(ChartConfig config) {
    ChartConfigEntity entity =
        new ChartConfigEntity(
            config.getId(),
            config.getTitle(),
            config.getChartType(),
            config.getOptionTemplate().toString());
    entity.setCreatedAt(config.getCreatedAt() != null ? config.getCreatedAt() : java.time.LocalDateTime.now());
    config
        .getBindings()
        .forEach(
            b -> {
              DataSourceBindingEntity bindingEntity =
                  new DataSourceBindingEntity(
                      b.getName(),
                      b.getDatasourceId(),
                      b.getQuery(),
                      b.getMappingPath(),
                      b.getBindingKey(),
                      b.isStream());
              bindingEntity.setChartConfig(entity);
              entity.getBindings().add(bindingEntity);
            });
    return entity;
  }
}
