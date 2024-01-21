package com.alebuc.lighter.repository;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import com.alebuc.lighter.entity.EventEntity;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;

public enum EventRepository {
    INSTANCE;

    private MongoCollection<EventEntity> collection;
    private final CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
    private final CodecRegistry codecRegistry = CodecRegistries.fromProviders(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
    private final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(EmbedMongoConfiguration.getInstance().getConnectionString())
            .codecRegistry(codecRegistry)
            .build();
    private final MongoClient mongoClient = MongoClients.create(mongoClientSettings);

    public void createCollection(String collectionName) {
        MongoDatabase database = mongoClient.getDatabase("Lighter");
        database.createCollection(collectionName);
        this.collection = database.getCollection(collectionName, EventEntity.class);
    }

    public void saveEvent(EventEntity eventEntity) {
        this.collection.insertOne(eventEntity);
    }

    public void saveEvents(List<EventEntity> eventEntities) {
        this.collection.insertMany(eventEntities);
    }
}
