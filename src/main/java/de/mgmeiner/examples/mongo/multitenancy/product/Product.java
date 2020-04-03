package de.mgmeiner.examples.mongo.multitenancy.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("products")
public class Product {
    @Id
    private String id;

    @Indexed
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
