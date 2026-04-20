package fpt.project.NeoNHS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NeoNhsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoNhsApplication.class, args);
	}
}
