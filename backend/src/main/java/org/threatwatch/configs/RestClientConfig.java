package org.threatwatch.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Primary
    @Bean("nvdRestClient")
    public RestClient nvdRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://services.nvd.nist.gov/rest/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean("githubRestClient")
    public RestClient githubRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://api.github.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
