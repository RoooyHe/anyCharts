package com.roy.anycharts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roy.anycharts.adapter.AdapterRegistry;
import com.roy.anycharts.adapter.impl.DatabaseAdapter;
import com.roy.anycharts.adapter.impl.MockAdapter;
import com.roy.anycharts.adapter.impl.RestAdapter;
import com.roy.anycharts.datasource.DatabaseConnectionStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AdapterConfig {
  
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
  
  @Bean
  public WebClient webClient() {
    return WebClient.builder().build();
  }
  
  @Bean
  public AdapterRegistry adapterRegistry(ObjectMapper mapper, WebClient webClient, DatabaseConnectionStore connectionStore) {
    AdapterRegistry r = new AdapterRegistry();
    r.register(new MockAdapter());
    r.register(new RestAdapter(webClient));
    r.register(new DatabaseAdapter(mapper, connectionStore));
    return r;
  }
}
