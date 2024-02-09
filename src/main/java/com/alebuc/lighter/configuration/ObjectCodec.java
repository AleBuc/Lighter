package com.alebuc.lighter.configuration;

import com.alebuc.lighter.entity.EventEntity;
import com.mongodb.MongoClientSettings;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.util.Map;

public class ObjectCodec implements CollectibleCodec<Object> {
    private final CodecRegistry codecRegistry;
    private final Codec<Document> codec;

    public ObjectCodec() {
        this.codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        this.codec = this.codecRegistry.get(Document.class);
    }

    public ObjectCodec(Codec<Document> codec) {
        this.codecRegistry = MongoClientSettings.getDefaultCodecRegistry();
        this.codec = codec;
    }

    public ObjectCodec(CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
        this.codec = this.codecRegistry.get(Document.class);
    }

    @Override
    public Object generateIdIfAbsentFromDocument(Object document) {
        if (documentHasId(document)) {
            return document;
        }
        return new ObjectId();
    }

    @Override
    public boolean documentHasId(Object document) {
        return document instanceof ObjectId;
    }

    @Override
    public BsonValue getDocumentId(Object document) {
        if (document instanceof ObjectId objectId) {
            return new BsonObjectId(objectId);
        }
        throw new IllegalStateException("Document must be an ObjectId.");
    }

    @Override
    public Object decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = codec.decode(reader, decoderContext);
        return EventConverter.convertToEvent(document);
    }

    @Override
    public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) {
        if (value instanceof Map<?,?> map) {

        }
        Document document = EventConverter.convertToDocument(value);
        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<Object> getEncoderClass() {
        return Object.class;
    }
}
