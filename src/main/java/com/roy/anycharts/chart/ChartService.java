package com.roy.anycharts.chart;


import com.roy.anycharts.adapter.AdapterRegistry;
import com.roy.anycharts.adapter.DataSourceAdapter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartService {
    private final AdapterRegistry registry;
    private final ChartConfigStore store;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChartService(AdapterRegistry registry, ChartConfigStore store) {
        this.registry = registry;
        this.store = store;
    }

    public Mono<JsonNode> renderChart(String id, Map<String,Object> variables) {
        Optional<ChartConfig> maybe = store.get(id);
        if (maybe.isEmpty()) return Mono.empty();
        ChartConfig cfg = maybe.get();
        List<Mono<Map.Entry<String, JsonNode>>> monos = new ArrayList<>();
        for (DataSourceBinding b : cfg.getBindings()) {
            DataSourceAdapter adapter = registry.get(b.getDatasourceId());
            if (adapter == null) return Mono.error(new RuntimeException("adapter not found: "+b.getDatasourceId()));
            Mono<JsonNode> dataMono = adapter.execute(b.getQuery(), variables);
            Mono<Map.Entry<String, JsonNode>> entryMono = dataMono.map(node -> Map.entry(b.getName(), node));
            monos.add(entryMono);
        }
        return Mono.zip(monos, arr -> Arrays.stream(arr)
                        .map(o -> (Map.Entry<String, JsonNode>) o)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .flatMap(map -> {
                    // 将 template 的占位符 {{binding:xxx}} 替换为对应 JSON 字符串
                    try {
                        String templateStr = mapper.writeValueAsString(cfg.getOptionTemplate());
                        for (DataSourceBinding b : cfg.getBindings()) {
                            // 基于 mapping.path 提取数组或值
                            Object extracted = JsonPath.read(map.get(b.getName()).toString(), b.getMappingPath());
                            String replacementJson = mapper.writeValueAsString(extracted);
                            templateStr = templateStr.replace("\"{{binding:"+b.getBindingKey()+"}}\"", replacementJson);
                        }
                        JsonNode finalOption = mapper.readTree(templateStr);
                        return Mono.just(finalOption);
                    } catch (Exception e) { return Mono.error(e); }
                });
    }

    // 简化：订阅示例（把第一个流数据合并成 Flux<RenderedChart>）
    public Flux<JsonNode> subscribeChart(String id, Map<String,Object> variables) {
        Optional<ChartConfig> maybe = store.get(id);
        if (maybe.isEmpty()) return Flux.empty();
        ChartConfig cfg = maybe.get();
        // 找到第一个 stream=true 的 binding，其他绑定用 execute 做静态聚合（demo 简化）
        List<DataSourceBinding> streaming = cfg.getBindings().stream().filter(DataSourceBinding::isStream).collect(Collectors.toList());
        if (streaming.isEmpty()) return Flux.empty();
        DataSourceBinding b = streaming.get(0);
        DataSourceAdapter adapter = registry.get(b.getDatasourceId());
        if (adapter == null) return Flux.error(new RuntimeException("adapter not found"));
        return adapter.subscribe(b.getQuery(), variables)
                .flatMap(node -> renderChart(id, variables).flux()); // 每次流更新重构一次（简化）
    }
}