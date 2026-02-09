package com.roy.anycharts.datasource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionStore {
  private final Map<String, DatabaseConnection> connections = new ConcurrentHashMap<>();

  public DatabaseConnectionStore() {
    // 添加默认的 H2 连接
    DatabaseConnection h2 = new DatabaseConnection(
        "h2-default",
        "H2 本地数据库",
        "jdbc:h2:file:./data/anycharts",
        "sa",
        "",
        "org.h2.Driver",
        true
    );
    connections.put(h2.getId(), h2);
  }

  public List<DatabaseConnection> getAll() {
    return new ArrayList<>(connections.values());
  }

  public Optional<DatabaseConnection> get(String id) {
    return Optional.ofNullable(connections.get(id));
  }

  public void save(DatabaseConnection connection) {
    connections.put(connection.getId(), connection);
  }

  public boolean delete(String id) {
    return connections.remove(id) != null;
  }
}
