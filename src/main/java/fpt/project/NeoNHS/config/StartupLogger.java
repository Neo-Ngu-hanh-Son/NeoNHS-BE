package fpt.project.NeoNHS.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port:8080}")
    private int serverPort;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String swaggerUrl = "http://localhost:" + serverPort + "/swagger-ui.html";
        
        log.info("");
        log.info("==============================================");
        log.info("  NeoNHS API is running!");
        log.info("  Swagger UI: {}", swaggerUrl);
        log.info("==============================================");
        log.info("");
    }
}
