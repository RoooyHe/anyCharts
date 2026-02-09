package com.roy.anycharts.adapter;

import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;

public interface DataSourceAdapter {
  Mono<JsonNode> execute(String query, Map<String, Object> params);

  Flux<JsonNode> subscribe(String query, Map<String, Object> params);

  String id();
}
