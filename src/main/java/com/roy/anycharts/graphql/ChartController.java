package com.roy.anycharts.graphql;


import com.roy.anycharts.chart.ChartService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

@Controller
public class ChartController {
    private final ChartService chartService;
    public ChartController(ChartService chartService) { this.chartService = chartService; }

    @QueryMapping
    public Mono<Map<String,Object>> renderChart(@Argument String id, @Argument Map<String,Object> variables) {
        return chartService.renderChart(id, variables)
                .map(json -> Map.of("id", id, "option", json));
    }

    @SubscriptionMapping
    public Flux<Map<String,Object>> chartUpdates(@Argument String id, @Argument Map<String,Object> variables) {
        return chartService.subscribeChart(id, variables)
                .map(json -> Map.of("id", id, "option", json));
    }
}