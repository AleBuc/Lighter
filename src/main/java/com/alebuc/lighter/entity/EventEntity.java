package com.alebuc.lighter.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.record.TimestampType;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Builder
public record EventEntity(
        Object key,
        Object value,
        int partition,
        long offset,
        Instant timestamp,
        Instant createTime,
        Instant logAppendTime,
        Map<String, String> headers
) {
    public static EventEntity fromConsumerRecord(ConsumerRecord<?, ?> consumerRecord) {
        EventEntityBuilder builder = EventEntity.builder()
                .key(mapData(consumerRecord.key()))
                .value(mapData(consumerRecord.value()))
                .partition(consumerRecord.partition())
                .offset(consumerRecord.offset())
                .headers(getHeaderMap(consumerRecord.headers()));
        if (consumerRecord.timestampType().equals(TimestampType.CREATE_TIME)) {
            builder.createTime(Instant.ofEpochSecond(consumerRecord.timestamp()));
        } else if (TimestampType.LOG_APPEND_TIME.equals(consumerRecord.timestampType())) {
            builder.logAppendTime(Instant.ofEpochSecond(consumerRecord.timestamp()));
        } else {
            builder.timestamp(Instant.ofEpochSecond(consumerRecord.timestamp()));
        }
        return builder.build();
    }

    private static Map<String, String> getHeaderMap(Headers headers) {
        Map<String, String> map = new HashMap<>();
        for (Header header : headers.toArray()) {
            map.put(header.key(), new String(header.value(), Charset.defaultCharset()));
        }
        return map;
    }

    private static Object mapData(Object object) {
        if (object instanceof String string) {
            return string ;
        } else {
            return convertToMap(object);
        }
    }

    private static Map<String, Object> convertToMap(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, new TypeReference<>() {});
    }
}