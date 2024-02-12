package com.alebuc.lighter.repository;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import com.alebuc.lighter.configuration.EventConverter;
import com.alebuc.lighter.configuration.EventEntityCodec;
import com.alebuc.lighter.entity.EventEntity;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;
import java.util.stream.Collectors;

public enum EventRepository {
    INSTANCE;

    private MongoCollection<Document> collection;
    private final EmbedMongoConfiguration embedMongoConfiguration = EmbedMongoConfiguration.INSTANCE;
    private final MongoClient mongoClient = setUpMongoClient();

    private MongoClient setUpMongoClient() {
        CodecRegistry codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        Codec<Document> documentCodec = codecRegistry.get(Document.class);
        Codec<EventEntity> eventEntityCodec = new EventEntityCodec(codecRegistry);
        // TODO https://stackoverflow.com/questions/41618203/mongodb-register-codecs-java with Object class
        codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(documentCodec, eventEntityCodec));
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(embedMongoConfiguration.getConnectionString())
                .codecRegistry(codecRegistry)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    public void createCollection(String collectionName) {

        MongoDatabase database = mongoClient.getDatabase("Lighter");
        database.createCollection(collectionName);
        this.collection = database.getCollection(collectionName, Document.class);
    }

//    public void saveEvent(EventEntity eventEntity) {
//        this.collection.insertOne(eventEntity);
//    }

    public void saveEvents(List<EventEntity> eventEntities) {
        List<Document> documents = eventEntities.stream().map(EventConverter::convertToDocument).collect(Collectors.toList());
        this.collection.insertMany(documents);
    }

    public boolean isCollectionCreated() {
        return collection != null;
    }
}
