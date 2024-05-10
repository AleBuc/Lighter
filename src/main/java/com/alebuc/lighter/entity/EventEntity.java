package com.alebuc.lighter.entity;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.record.TimestampType;
import org.json.JSONException;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document
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
        //fixme bad time format
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
            try {
                return convertToMap(string);
            } catch (Exception e) {
                return string ;
            }
        } else {
            return object;
        }
    }

    private static Map<String, Object> convertToMap(String s) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(s, new TypeReference<HashMap<String, Object>>() {});
    }
}