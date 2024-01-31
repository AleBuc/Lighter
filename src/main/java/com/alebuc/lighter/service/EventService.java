package com.alebuc.lighter.service;

import com.alebuc.lighter.entity.EventEntity;
import com.alebuc.lighter.repository.EventRepository;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void saveEvents(ConsumerRecords<Object, Object> records) {
        Optional<String> topicName = records.partitions().stream().map(TopicPartition::topic).distinct().findFirst();
        if (topicName.isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null in records.");
        }
        eventRepository.createCollection(topicName.get());
        List<EventEntity> eventEntities = new ArrayList<>();
        for (ConsumerRecord<Object, Object> record : records) {
            log.info("New event! Partition: {}, Offset: {}", record.partition(), record.offset());
            eventEntities.add(EventEntity.fromConsumerRecord(record));
        }
        eventRepository.saveEvents(eventEntities);
    }
}
