package uk.ac.ed.acp.cw1.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableScheduling
public class IlpRestServiceConfig {
@Bean
    public String ilpEndpoint(){
    String endpoint = System.getenv("ILP_ENDPOINT");
    if (endpoint == null || endpoint.isEmpty()) {
        endpoint = "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net";
    }
    return endpoint;
}

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(10))
            .build();
    }

}
