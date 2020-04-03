package de.mgmeiner.examples.mongo.multitenancy.product;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("products")
public class Product {
    private String id;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
