package com.roy.anycharts.chart;

public class DataSourceBinding {
  private String name;
  private String datasourceId;
  private String query;
  private String mappingPath;
  private String bindingKey;
  private boolean stream;

  public DataSourceBinding() {}

  public DataSourceBinding(
      String name,
      String datasourceId,
      String query,
      String mappingPath,
      String bindingKey,
      boolean stream) {
    this.name = name;
    this.datasourceId = datasourceId;
    this.query = query;
    this.mappingPath = mappingPath;
    this.bindingKey = bindingKey;
    this.stream = stream;
  }

  // getters / setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDatasourceId() {
    return datasourceId;
  }

  public void setDatasourceId(String datasourceId) {
    this.datasourceId = datasourceId;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getMappingPath() {
    return mappingPath;
  }

  public void setMappingPath(String mappingPath) {
    this.mappingPath = mappingPath;
  }

  public String getBindingKey() {
    return bindingKey;
  }

  public void setBindingKey(String bindingKey) {
    this.bindingKey = bindingKey;
  }

  public boolean isStream() {
    return stream;
  }

  public void setStream(boolean stream) {
    this.stream = stream;
  }
}
