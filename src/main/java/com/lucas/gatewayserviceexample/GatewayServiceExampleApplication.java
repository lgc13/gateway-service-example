package com.lucas.gatewayserviceexample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableZuulProxy
@SpringBootApplication
public class GatewayServiceExampleApplication {

	@Value("${DOG_APP_SERVER}")
	private String DOG_APP_SERVER;

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceExampleApplication.class, args);
	}

	@Bean
	public SimpleFilter simpleFilter() {
		return new SimpleFilter();
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(DOG_APP_SERVER)
						.allowedMethods("GET", "POST");
			}
		};
	}
}
