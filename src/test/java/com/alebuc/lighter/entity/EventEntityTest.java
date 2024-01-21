package com.alebuc.lighter.entity;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class EventEntityTest {

    @Test
    public void shouldCreateEventEntity_stringKey_stringValue() {
        //GIVEN
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "testTopic",
                0 ,
                1,
                1705838400,
                TimestampType.CREATE_TIME,
                0,
                0,
                "keyTest",
                "valueTest",
                new RecordHeaders(List.of(new RecordHeader("keyHeader", "value".getBytes()))),
                null);
        //WHEN
        EventEntity eventEntity = EventEntity.fromConsumerRecord(record);
        //THEN
        assertThat(eventEntity)
                .isNotNull()
                .hasFieldOrPropertyWithValue("key", "keyTest")
                .hasFieldOrPropertyWithValue("value", "valueTest")
                .hasFieldOrPropertyWithValue("partition", 0)
                .hasFieldOrPropertyWithValue("offset", 1L)
                .hasFieldOrPropertyWithValue("createTime", Instant.parse("2024-01-21T12:00:00Z"))
                .hasFieldOrPropertyWithValue("headers", Map.of("keyHeader", "value"));
    }

}
