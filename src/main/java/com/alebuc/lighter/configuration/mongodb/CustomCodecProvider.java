package com.alebuc.lighter.configuration.mongodb;

import lombok.RequiredArgsConstructor;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CustomCodecProvider implements CodecProvider {
    private final Codec<Object> objectCodec = new ObjectCodec();
    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (aClass == Object.class) {
            return (Codec<T>) objectCodec;
        }
        return null;
    }
}
