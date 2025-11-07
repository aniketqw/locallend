package com.locallend.locallend.config;

import com.locallend.locallend.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;


/**
 * Ensures required MongoDB text indexes exist at startup so that search endpoints work
 * on a freshly created database without manual intervention.
 */
@Component
@Profile("prod") // limit to prod profile used in docker-compose
public class MongoTextIndexInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(MongoTextIndexInitializer.class);
    private final MongoTemplate mongoTemplate;

    public MongoTextIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureCategoryTextIndex();
    }

    private void ensureCategoryTextIndex() {
    TextIndexDefinition def = new TextIndexDefinition.TextIndexDefinitionBuilder()
        .onField("name")
        .onField("description")
        .build();
    String indexName = mongoTemplate.indexOps(Category.class).createIndex(def);
    log.info("[MongoIndex] Ensured Category text index (name='{}') on fields: name, description", indexName);
    }
}
