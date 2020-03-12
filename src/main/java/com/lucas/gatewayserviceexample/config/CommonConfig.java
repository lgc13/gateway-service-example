package com.lucas.gatewayserviceexample.config;

import com.lucas.gatewayserviceexample.filters.ZuulRequestsFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CommonConfig {

    @Value("${dog.app.url}")
    private String dogAppUrl;

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(dogAppUrl)
                        .allowedMethods("GET", "POST");
            }
        };
    }

    @Bean
    public ZuulRequestsFilter simpleFilter() {
        return new ZuulRequestsFilter();
    }
}
