package com.alebuc.lighter.configuration;

import com.alebuc.lighter.entity.EventEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import lombok.NoArgsConstructor;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

public class EventEntityCodec implements CollectibleCodec<EventEntity> {
    private final CodecRegistry codecRegistry;
    private final Codec<Document> codec;

    public EventEntityCodec() {
        this.codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        this.codec = this.codecRegistry.get(Document.class);
    }

    public EventEntityCodec(Codec<Document> codec) {
        this.codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        this.codec = codec;
    }

    public EventEntityCodec(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
        this.codec = this.codecRegistry.get(Document.class);
    }

    @Override
    public EventEntity generateIdIfAbsentFromDocument(EventEntity document) {
        if (documentHasId(document)) {
            return document;
        }
        return document.withId(new ObjectId());
    }

    @Override
    public boolean documentHasId(EventEntity document) {
        return document.id() != null;
    }

    @Override
    public BsonValue getDocumentId(EventEntity document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("Document doesn't contain _id.");
        }
        return new BsonObjectId(document.id());
    }

    @Override
    public EventEntity decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = codec.decode(reader, decoderContext);
        return EventConverter.convertToEvent(document);
    }

    @Override
    public void encode(BsonWriter writer, EventEntity value, EncoderContext encoderContext) {
        Document document =  EventConverter.convertToDocument(value);
        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<EventEntity> getEncoderClass() {
        return EventEntity.class;
    }
}
