package com.roy.anycharts.datasource;

import java.sql.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMetadataService {
  private final DatabaseConnectionStore connectionStore;

  // 获取数据库的所有表
  public Mono<List<String>> getTables(String connectionId) {
    return Mono.fromCallable(() -> {
      DatabaseConnection conn = connectionStore.get(connectionId)
          .orElseThrow(() -> new IllegalArgumentException("数据库连接不存在: " + connectionId));

      List<String> tables = new ArrayList<>();
      try (Connection connection = DriverManager.getConnection(conn.getJdbcUrl(), conn.getUsername(), conn.getPassword())) {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        while (rs.next()) {
          tables.add(rs.getString("TABLE_NAME"));
        }
      }
      return tables;
    });
  }

  // 获取表的所有字段
  public Mono<List<Map<String, Object>>> getColumns(String connectionId, String tableName) {
    return Mono.fromCallable(() -> {
      DatabaseConnection conn = connectionStore.get(connectionId)
          .orElseThrow(() -> new IllegalArgumentException("数据库连接不存在: " + connectionId));

      List<Map<String, Object>> columns = new ArrayList<>();
      try (Connection connection = DriverManager.getConnection(conn.getJdbcUrl(), conn.getUsername(), conn.getPassword())) {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getColumns(null, null, tableName, "%");
        while (rs.next()) {
          Map<String, Object> column = new HashMap<>();
          column.put("name", rs.getString("COLUMN_NAME"));
          column.put("type", rs.getString("TYPE_NAME"));
          column.put("size", rs.getInt("COLUMN_SIZE"));
          column.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
          columns.add(column);
        }
      }
      return columns;
    });
  }

  // 预览表数据
  public Mono<List<Map<String, Object>>> previewData(String connectionId, String tableName, int limit) {
    return Mono.fromCallable(() -> {
      DatabaseConnection conn = connectionStore.get(connectionId)
          .orElseThrow(() -> new IllegalArgumentException("数据库连接不存在: " + connectionId));

      List<Map<String, Object>> rows = new ArrayList<>();
      String sql = "SELECT * FROM " + tableName + " LIMIT " + limit;
      
      try (Connection connection = DriverManager.getConnection(conn.getJdbcUrl(), conn.getUsername(), conn.getPassword());
           Statement stmt = connection.createStatement();
           ResultSet rs = stmt.executeQuery(sql)) {
        
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
          Map<String, Object> row = new HashMap<>();
          for (int i = 1; i <= columnCount; i++) {
            row.put(metaData.getColumnName(i), rs.getObject(i));
          }
          rows.add(row);
        }
      }
      return rows;
    });
  }
}
