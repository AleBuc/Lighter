package com.alebuc.lighter.configuration.mongodb;

import lombok.extern.slf4j.Slf4j;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ObjectCodec implements Codec<Object> {
    @Override
    public Object decode(BsonReader bsonReader, DecoderContext decoderContext) {
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
            case HashMap<?, ?> hm -> {
                bsonWriter.writeStartDocument();
                for (Map.Entry<?, ?> entry : hm.entrySet()) {
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
            case null -> bsonWriter.writeNull();
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
                        log.error(e.getMessage());
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
