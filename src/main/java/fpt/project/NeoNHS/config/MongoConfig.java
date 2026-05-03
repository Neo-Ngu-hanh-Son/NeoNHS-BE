package fpt.project.NeoNHS.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "fpt.project.NeoNHS.repository.mongo")
public class MongoConfig {

    /**
     * Disable writing the '_class' type discriminator for ALL embedded documents.
     *
     * ROOT CAUSE FIX: Spring Data MongoDB writes '_class' (e.g.
     * "com.fasterxml.jackson.databind.node.ArrayNode") for non-simple object
     * values stored inside Map<String,Object> fields (like ChatMessage.metadata).
     * On read-back, it calls BeanUtils.instantiateClass(ArrayNode.class) which
     * fails with BeanInstantiationException because ArrayNode has no no-arg
     * constructor.
     *
     * Setting DefaultMongoTypeMapper(null) stops writing '_class' for embedded
     * documents. Existing documents that already have '_class' are also handled
     * because the type mapper no longer tries to resolve them.
     */
    @Autowired
    public void disableTypeMapper(MappingMongoConverter converter) {
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}
