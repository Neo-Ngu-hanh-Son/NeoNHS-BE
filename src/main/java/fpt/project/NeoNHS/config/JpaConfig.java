package fpt.project.NeoNHS.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "fpt.project.NeoNHS.repository",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "fpt\\.project\\.NeoNHS\\.repository\\.mongo\\..*"
        )
)
public class JpaConfig {
}
