package com.roy.anycharts.adapter.impl;

import com.roy.anycharts.adapter.DataSourceAdapter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

public class MockAdapter implements DataSourceAdapter {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Mono<JsonNode> execute(String query, Map<String, Object> params) {
        // query 可以是 "mock:sales" 等，简单返回模拟数据
        try {
            String json = "{\"items\":[{\"date\":\"2026-01-01\",\"value\":100},{\"date\":\"2026-01-02\",\"value\":150}]}";
            return Mono.just(mapper.readTree(json));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public Flux<JsonNode> subscribe(String query, Map<String, Object> params) {
        // 每2秒发一条增量，用于 demo subscription
        return Flux.interval(Duration.ofSeconds(2))
                .map(i -> {
                    try {
                        String json = String.format("{\"items\":[{\"date\":\"2026-01-%02d\",\"value\":%d}]}", 3 + i.intValue(), 100 + i.intValue() * 10);
                        return mapper.readTree(json);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public String id() { return "mock-adapter"; }
}