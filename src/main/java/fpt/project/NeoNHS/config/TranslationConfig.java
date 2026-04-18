package fpt.project.NeoNHS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TranslationConfig {

    @Value("${translation.endpoint}")
    private String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    @Bean(name = "translationRestTemplate")
    public RestTemplate translationRestTemplate() {
        return new RestTemplate();
    }
}
