package com.alebuc.lighter.service;

import com.alebuc.lighter.configuration.kafka.KafkaConfiguration;
import com.alebuc.lighter.entity.EventEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Kafka consumption service
 *
 * @author AleBuc
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

    @Getter
    private boolean isListening = false;
    private final KafkaConfiguration kafkaConfiguration;
    private final Map<String, KafkaConsumer<Object, Object>> consumerMap = new HashMap<>();
    @Getter
    private final List<KafkaMessageListenerContainer<Object, Object>> containers = new ArrayList<>();
    private final DefaultKafkaConsumerFactory<Object, Object> defaultKafkaConsumerFactory;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates and adds a kafka topic consumer to {@link KafkaMessageListenerContainer}.
     * @param topic topic name to add
     */
    public void addTopicConsumer(String topic) {
        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setAssignmentCommitOption(ContainerProperties.AssignmentCommitOption.NEVER);
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);
        KafkaMessageListenerContainer<Object, Object> kafkaMessageListenerContainer = new KafkaMessageListenerContainer<>(defaultKafkaConsumerFactory, containerProperties);
        BlockingQueue<ConsumerRecord<Object, Object>> records = new LinkedBlockingQueue<>();
        kafkaMessageListenerContainer.setupMessageListener((MessageListener<Object, Object>) message -> {
            log.info("New event! Key: {}, Value: {}", message.key(), message.value());
            EventEntity eventEntity = EventEntity.fromConsumerRecord(message, objectMapper);
            getMongoCollection(topic).insertOne(eventEntity);
            records.add(message);
        });
        kafkaMessageListenerContainer.start();
        containers.add(kafkaMessageListenerContainer);

    }

    /**
     * Stops all the consumers of the container.
     */
    public void stopListener() {
        containers.forEach(AbstractMessageListenerContainer::stop);
    }

    private MongoCollection<EventEntity> getMongoCollection(String topic) {
        MongoDatabase database = mongoTemplate.getDb();
        Set<String> collectionNames = mongoTemplate.getCollectionNames();
        if (!collectionNames.contains(topic)) {
            database.createCollection(topic);
        }
        return database.getCollection(topic, EventEntity.class);
    }
}
