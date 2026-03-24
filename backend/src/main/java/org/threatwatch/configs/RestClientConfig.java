package org.threatwatch.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient nvdRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://services.nvd.nist.gov/rest/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
