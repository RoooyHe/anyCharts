package com.roy.anycharts.adapter.impl;

import com.roy.anycharts.adapter.DataSourceAdapter;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class RestAdapter implements DataSourceAdapter {
  private final WebClient client = WebClient.create();
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Mono<JsonNode> execute(String query, Map<String, Object> params) {
    // query 作为 URL 模板（简化示例），params 可用于拼接
    return client
        .get()
        .uri(query)
        .retrieve()
        .bodyToMono(String.class)
        .handle(
            (s, sink) -> {
              try {
                sink.next(mapper.readTree(s));
              } catch (Exception e) {
                sink.error(new RuntimeException(e));
              }
            });
  }

  @Override
  public Flux<JsonNode> subscribe(String query, Map<String, Object> params) {
    // RestAdapter 不支持流式，返回 empty
    return Flux.empty();
  }

  @Override
  public String id() {
    return "rest-adapter";
  }
}
