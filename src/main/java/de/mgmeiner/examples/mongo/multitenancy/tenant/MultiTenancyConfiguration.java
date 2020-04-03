package de.mgmeiner.examples.mongo.multitenancy.tenant;


import com.mongodb.MongoClientSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.ReactiveMongoClientFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(TenantProperties.class)
public class MultiTenancyConfiguration {

    @Bean
    public MultiTenancyReactiveMongoTemplate reactiveMongoTemplate(
            TenantProperties tenantProperties,
            Environment environment,
            MongoConverter mongoConverter,
            ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers,
            ObjectProvider<MongoClientSettings> settings) {

        var multiTenancyReactiveMongoDatabaseFactories = tenantProperties.getTenants().stream().map(t -> {
            var mongoProperties = t.getMongo();

            var factory = new ReactiveMongoClientFactory(mongoProperties, environment, builderCustomizers.orderedStream().collect(Collectors.toList()));
            var mongoClient = factory.createMongoClient(settings.getIfAvailable());
            return new MultiTenancyReactiveMongoDatabaseFactory(t.getId(), mongoClient, mongoProperties.getDatabase());
        }).collect(Collectors.toList());

        return new MultiTenancyReactiveMongoTemplate(multiTenancyReactiveMongoDatabaseFactories, mongoConverter);
    }

    @Bean
    public TenantExtractingWebFilter tenantExtractingWebFilter(TenantProperties tenantProperties) {
        return new TenantExtractingWebFilter(tenantProperties
                .getTenants()
                .stream()
                .map(TenantProperties.Tenant::getId)
                .collect(Collectors.toList()));
    }
}
