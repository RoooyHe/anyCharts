package com.roy.anycharts.datasource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConnection {
  private String id;
  private String name;
  private String jdbcUrl;
  private String username;
  private String password;
  private String driverClass;
  private boolean active;
}
