package com.medina.heritage.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory; // <--- IMPORTEZ CECI
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration for HTTP clients used in Salesforce API calls.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // ON CHANGE ICI : On utilise HttpComponentsClientHttpRequestFactory au lieu de
        // SimpleClientHttpRequestFactory
        // Cela permet de supporter la méthode PATCH
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
                new HttpComponentsClientHttpRequestFactory());

        return new RestTemplate(factory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}