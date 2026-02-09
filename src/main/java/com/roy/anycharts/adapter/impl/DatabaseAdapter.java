package com.roy.anycharts.adapter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.roy.anycharts.adapter.DataSourceAdapter;
import com.roy.anycharts.datasource.DatabaseConnection;
import com.roy.anycharts.datasource.DatabaseConnectionStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseAdapter implements DataSourceAdapter {
  private final ObjectMapper mapper;
  private final DatabaseConnectionStore connectionStore;

  @Override
  public String id() {
    return "database-adapter";
  }

  @Override
  public Mono<JsonNode> execute(String query, Map<String, Object> variables) {
    return Mono.fromCallable(() -> {
      log.info("执行数据库查询: {}", query);
      
      // 解析查询配置 (格式: connectionId:tableName 或 connectionId:SELECT...)
      String[] parts = query.split(":", 2);
      if (parts.length < 2) {
        throw new IllegalArgumentException("数据库查询格式错误，应为: connectionId:tableName 或 connectionId:SQL");
      }
      
      String connectionId = parts[0];
      String queryPart = parts[1];
      
      DatabaseConnection conn = connectionStore.get(connectionId)
          .orElseThrow(() -> new IllegalArgumentException("数据库连接不存在: " + connectionId));
      
      // 判断是表名还是 SQL
      String sql;
      if (queryPart.toUpperCase().trim().startsWith("SELECT")) {
        sql = queryPart;
      } else {
        // 简单表名查询
        sql = "SELECT * FROM " + queryPart;
      }
      
      // 替换 SQL 中的变量
      String processedSql = sql;
      if (variables != null) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
          processedSql = processedSql.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
      }
      
      log.info("执行 SQL: {}", processedSql);
      
      // 执行查询
      try (Connection connection = DriverManager.getConnection(conn.getJdbcUrl(), conn.getUsername(), conn.getPassword());
           PreparedStatement stmt = connection.prepareStatement(processedSql);
           ResultSet rs = stmt.executeQuery()) {
        
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        ArrayNode items = mapper.createArrayNode();
        
        while (rs.next()) {
          ObjectNode row = mapper.createObjectNode();
          for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            
            if (value == null) {
              row.putNull(columnName);
            } else if (value instanceof Number) {
              if (value instanceof Integer) {
                row.put(columnName, (Integer) value);
              } else if (value instanceof Long) {
                row.put(columnName, (Long) value);
              } else if (value instanceof Double) {
                row.put(columnName, (Double) value);
              } else {
                row.put(columnName, value.toString());
              }
            } else if (value instanceof Boolean) {
              row.put(columnName, (Boolean) value);
            } else {
              row.put(columnName, value.toString());
            }
          }
          items.add(row);
        }
        
        ObjectNode result = mapper.createObjectNode();
        result.set("items", items);
        result.put("count", items.size());
        
        log.info("查询返回 {} 条记录", items.size());
        return (JsonNode) result;
      }
    });
  }

  @Override
  public Flux<JsonNode> subscribe(String query, Map<String, Object> variables) {
    // 数据库查询不支持流式，返回单次查询结果
    return execute(query, variables).flux();
  }
}
