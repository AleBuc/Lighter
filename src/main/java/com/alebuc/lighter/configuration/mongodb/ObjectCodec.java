package com.alebuc.lighter.configuration.mongodb;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjectCodec implements Codec<Object> {
    @Override
    public Object decode(BsonReader bsonReader, DecoderContext decoderContext) {
        return null;
    }

    private Object read(BsonReader bsonReader) {
        BsonType bsonType = bsonReader.getCurrentBsonType();
        switch (bsonType) {
            case ARRAY -> {
                ArrayList<Object> arrayList = new ArrayList<>();
                bsonReader.readStartArray();
                while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    arrayList.add(read(bsonReader));
                }
                bsonReader.readEndArray();
                return arrayList;
            }
            case DOCUMENT -> {
                HashMap<String, Object> document = new HashMap<>();
                bsonReader.readStartDocument();
                while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    document.put(bsonReader.readName(), read(bsonReader));
                }
                bsonReader.readEndDocument();
                return document;
            }
            case INT32 -> bsonReader.readInt32();

            case INT64 -> bsonReader.readInt64();

            case STRING -> bsonReader.readString();

            case BOOLEAN -> bsonReader.readBoolean();

            case BINARY -> bsonReader.readBinaryData();

            case DATE_TIME -> bsonReader.readDateTime();

            case DOUBLE -> bsonReader.readDouble();

            case DECIMAL128 -> bsonReader.readDecimal128();

            case OBJECT_ID -> bsonReader.readObjectId();

            case TIMESTAMP -> bsonReader.readTimestamp();

            default -> {
                return null;
            }
        }
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, Object o, EncoderContext encoderContext) {
        write(bsonWriter, o);
    }

    private void write(BsonWriter bsonWriter, Object o) {
        switch (o) {
            case String string -> bsonWriter.writeString(string);
            case Boolean bool -> bsonWriter.writeBoolean(bool);
            case Integer integer -> bsonWriter.writeInt32(integer);
            case Long value -> bsonWriter.writeInt64(value);
            case BigDecimal bigDecimal -> bsonWriter.writeDecimal128(Decimal128.parse(bigDecimal.toString()));
            case Double dbl -> bsonWriter.writeDouble(dbl);
            case Float flt -> bsonWriter.writeDouble(flt);
            case HashMap<?,?> hm -> {
                bsonWriter.writeStartDocument();
                for (Map.Entry<?,?> entry : hm.entrySet()) {
                    String key = entry.getKey().toString();
                    Object value = entry.getValue();
                    bsonWriter.writeName(key);
                    write(bsonWriter, value);
                }
                bsonWriter.writeEndDocument();
            }
            case ArrayList<?> arrayList -> {
                bsonWriter.writeStartArray();
                for (Object item : arrayList) {
                    write(bsonWriter, item);
                }
                bsonWriter.writeEndArray();
            }
            default -> {
                if (o.getClass().isArray()) {
                    bsonWriter.writeStartArray();
                    int length = Array.getLength(o);
                    for (int i = 0; i < length; i++) {
                        Object item = Array.get(o, i);
                        write(bsonWriter, item);
                    }
                    bsonWriter.writeEndArray();
                } else {
                    try {
                        Class<?> clazz = o.getClass();
                        bsonWriter.writeStartDocument();
                        while (clazz != null) {
                            Field[] fields = clazz.getDeclaredFields();
                            for (Field field : fields) {
                                int modifiers = field.getModifiers();
                                if (!Modifier.isFinal(modifiers)) {
                                    field.setAccessible(true);
                                    String fieldName = field.getName();
                                    if (fieldName.equals("id")) {
                                        fieldName = "_id";
                                    }
                                    Object fieldValue = field.get(o);
                                    bsonWriter.writeName(fieldName);
                                    write(bsonWriter, fieldValue);
                                }
                            }
                            clazz = clazz.getSuperclass();
                        }
                        bsonWriter.writeEndDocument();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public Class<Object> getEncoderClass() {
        return Object.class;
    }
}
