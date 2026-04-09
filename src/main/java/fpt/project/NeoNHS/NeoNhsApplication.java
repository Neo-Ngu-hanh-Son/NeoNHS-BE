package fpt.project.NeoNHS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "fpt.project.NeoNHS.repository", excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.REGEX, pattern = "fpt\\.project\\.NeoNHS\\.repository\\.mongo\\..*"))
public class NeoNhsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoNhsApplication.class, args);
	}
}
