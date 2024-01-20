package com.alebuc.lighter.repository;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public enum EventRepository {
    INSTANCE;

    private MongoCollection<?> collection;
    private final MongoClient mongoClient = MongoClients.create(EmbedMongoConfiguration.getInstance().getConnectionString());

    public void createCollection(String collectionName) {
        MongoDatabase database = mongoClient.getDatabase("Lighter");
        database.createCollection(collectionName);
        this.collection = database.getCollection(collectionName);
    }
}
