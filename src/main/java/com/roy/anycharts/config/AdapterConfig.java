package com.roy.anycharts.config;


import com.roy.anycharts.adapter.AdapterRegistry;
import com.roy.anycharts.adapter.impl.MockAdapter;
import com.roy.anycharts.adapter.impl.RestAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdapterConfig {
    @Bean
    public AdapterRegistry adapterRegistry() {
        AdapterRegistry r = new AdapterRegistry();
        r.register(new MockAdapter());
        r.register(new RestAdapter());
        return r;
    }
}