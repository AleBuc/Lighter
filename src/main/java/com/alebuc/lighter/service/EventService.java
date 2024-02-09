package com.alebuc.lighter.service;

import com.alebuc.lighter.entity.EventEntity;
import com.alebuc.lighter.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public enum EventService {
    INSTANCE;

//    private final EventRepository eventRepository = EventRepository.INSTANCE;

    public void saveEvents(ConsumerRecords<Object, Object> records, String topicName) {
        if (StringUtils.isBlank(topicName)) {
            throw new IllegalArgumentException("Topic name cannot be null in records.");
        }
        EventRepository.INSTANCE.createCollection(topicName);
        List<EventEntity> eventEntities = new ArrayList<>();
        for (ConsumerRecord<Object, Object> record : records) {
            log.info("New event! Partition: {}, Offset: {}", record.partition(), record.offset());
            eventEntities.add(EventEntity.fromConsumerRecord(record));
        }
        EventRepository.INSTANCE.saveEvents(eventEntities);
    }
}
