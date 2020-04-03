package de.mgmeiner.examples.mongo.multitenancy.tenant;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

public final class MultiTenancyReactiveMongoDatabaseFactory extends SimpleReactiveMongoDatabaseFactory {

    private String tenantId;

    public MultiTenancyReactiveMongoDatabaseFactory(String tenantId, MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
