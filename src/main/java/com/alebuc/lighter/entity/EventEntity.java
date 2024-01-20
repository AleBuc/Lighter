package com.alebuc.lighter.entity;

import lombok.Builder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.record.TimestampType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Builder
public record EventEntity<K, V>(
        K key,
        V value,
        int partition,
        long offset,
        Instant timestamp,
        Instant createTime,
        Instant logAppendTime,
        Map<String, byte[]> headers
) {
    public EventEntity<K, V> fromConsumerRecord(ConsumerRecord<K, V> consumerRecord) {
        EventEntityBuilder<K, V> builder = EventEntity.<K, V>builder()
                .key(consumerRecord.key())
                .value(consumerRecord.value())
                .partition(consumerRecord.partition())
                .offset(consumerRecord.offset())
                .headers(getHeaderMap(consumerRecord.headers()));
        if (consumerRecord.timestampType().equals(TimestampType.CREATE_TIME)) {
            builder.createTime(Instant.ofEpochMilli(consumerRecord.timestamp()));
        } else if (TimestampType.LOG_APPEND_TIME.equals(consumerRecord.timestampType())) {
            builder.logAppendTime(Instant.ofEpochMilli(consumerRecord.timestamp()));
        } else {
            builder.timestamp(Instant.ofEpochMilli(consumerRecord.timestamp()));
        }
        return builder.build();
    }

    private Map<String, byte[]> getHeaderMap(Headers headers) {
        Map<String, byte[]> map = new HashMap<>();
        for (Header header : headers.toArray()) {
            map.put(header.key(), header.value());
        }
        return map;
    }
}
