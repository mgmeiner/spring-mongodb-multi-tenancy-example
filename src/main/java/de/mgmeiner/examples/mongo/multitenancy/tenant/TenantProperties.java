package de.mgmeiner.examples.mongo.multitenancy.tenant;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("multi-tenancy")
public class TenantProperties {
    private List<Tenant> tenants = new ArrayList<>();

    static class Tenant {
        private String id;
        private MongoProperties mongo;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public MongoProperties getMongo() {
            return mongo;
        }

        public void setMongo(MongoProperties mongo) {
            this.mongo = mongo;
        }
    }

    public List<Tenant> getTenants() {
        return tenants;
    }

    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
    }
}
