package com.roy.anycharts.chart;

import com.jayway.jsonpath.JsonPath;
import com.roy.anycharts.adapter.AdapterRegistry;
import com.roy.anycharts.adapter.DataSourceAdapter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ChartService {
  private final AdapterRegistry registry;
  private final ChartConfigStore store;
  private final ObjectMapper mapper = new ObjectMapper();

  public ChartService(AdapterRegistry registry, ChartConfigStore store) {
    this.registry = registry;
    this.store = store;
  }

  public Mono<Map<String, Object>> renderChart(String id, Map<String, Object> variables) {
    Optional<ChartConfig> maybe = store.get(id);
    if (maybe.isEmpty()) return Mono.empty();
    ChartConfig cfg = maybe.get();

    List<Mono<Map.Entry<String, JsonNode>>> monos = new ArrayList<>();
    for (DataSourceBinding b : cfg.getBindings()) {
      DataSourceAdapter adapter = registry.get(b.getDatasourceId());
      if (adapter == null)
        return Mono.error(new RuntimeException("adapter not found: " + b.getDatasourceId()));
      Mono<JsonNode> dataMono = adapter.execute(b.getQuery(), variables);
      Mono<Map.Entry<String, JsonNode>> entryMono =
          dataMono.map(node -> Map.entry(b.getName(), node));
      monos.add(entryMono);
    }

    return Mono.zip(
            monos,
            arr ->
                Arrays.stream(arr)
                    .map(o -> (Map.Entry<String, JsonNode>) o)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
        .flatMap(
            map -> {
              try {
                // 把 template 序列化为字符串，然后替换被双引号包着的占位符（例如 "\"{{binding:series1}}\""）
                String templateStr = mapper.writeValueAsString(cfg.getOptionTemplate());
                for (DataSourceBinding b : cfg.getBindings()) {
                  JsonNode dataNode = map.get(b.getName());
                  Object extracted = JsonPath.read(dataNode.toString(), b.getMappingPath());
                  String replacementJson = mapper.writeValueAsString(extracted);
                  // 替换被双引号包住的占位符
                  templateStr =
                      templateStr.replace(
                          "\"{{binding:" + b.getBindingKey() + "}}\"", replacementJson);
                }
                JsonNode finalOption = mapper.readTree(templateStr);
                Map<String, Object> result = new HashMap<>();
                result.put("id", id);
                result.put("option", mapper.convertValue(finalOption, Map.class));
                return Mono.just(result);
              } catch (Exception e) {
                return Mono.error(e);
              }
            });
  }

  // 简化订阅：如果有任何 binding 是 stream=true，会使用该 adapter.subscribe 并在每个事件时重新触发 renderChart
  public Flux<Map<String, Object>> subscribeChart(String id, Map<String, Object> variables) {
    Optional<ChartConfig> maybe = store.get(id);
    if (maybe.isEmpty()) return Flux.empty();
    ChartConfig cfg = maybe.get();
    List<DataSourceBinding> streaming =
        cfg.getBindings().stream().filter(DataSourceBinding::isStream).toList();
    if (streaming.isEmpty()) return Flux.empty();
    DataSourceBinding b = streaming.get(0);
    DataSourceAdapter adapter = registry.get(b.getDatasourceId());
    if (adapter == null) return Flux.error(new RuntimeException("adapter not found"));
    return adapter
        .subscribe(b.getQuery(), variables)
        .flatMap(evt -> renderChart(id, variables).flux());
  }
}
