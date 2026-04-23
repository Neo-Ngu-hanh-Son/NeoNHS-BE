package fpt.project.NeoNHS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ElevenLabsConfig {
    @Value("${elevenlabs.key}")
    private String apiKey;

    @Value("${elevenlabs.base-url}")
    private String baseUrl;

    @Bean
    public RestClient elevenLabsRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("xi-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean RestClient elevenLabsForcedAlignmentRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("xi-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json; charset=utf-8")
                .build();
    }
}
