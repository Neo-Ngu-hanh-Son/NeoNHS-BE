package fpt.project.NeoNHS.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    /**
     * Primary ObjectMapper bean used across the entire application.
     * - JavaTimeModule: supports LocalDateTime / ZonedDateTime serialization
     * - FAIL_ON_UNKNOWN_PROPERTIES = false: tolerates extra fields from MongoDB / OpenAI
     * - WRITE_DATES_AS_TIMESTAMPS = false: ISO-8601 date strings
     * NOTE: Do NOT call new ObjectMapper() anywhere else in the codebase.
     *       Always inject this bean via @RequiredArgsConstructor or @Autowired.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
