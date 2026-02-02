package com.roy.anycharts.adapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdapterRegistry {
  private final Map<String, DataSourceAdapter> adapters = new ConcurrentHashMap<>();
  public void register(DataSourceAdapter adapter) { adapters.put(adapter.id(), adapter); }
  public DataSourceAdapter get(String id) { return adapters.get(id); }
}