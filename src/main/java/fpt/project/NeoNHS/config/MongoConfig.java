package fpt.project.NeoNHS.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "fpt.project.NeoNHS.repository.mongo")
public class MongoConfig {
    // MongoDB repositories are scanned only from the 'mongo' sub-package
    // to avoid conflicts with JPA repositories.
}
