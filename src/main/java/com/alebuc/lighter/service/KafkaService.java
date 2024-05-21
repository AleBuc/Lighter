package com.alebuc.lighter.service;

import com.alebuc.lighter.configuration.kafka.KafkaConfiguration;
import com.alebuc.lighter.entity.EventEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
    private final Map<String, KafkaMessageListenerContainer<Object, Object>> containersMap = new HashMap<>();
    private final DefaultKafkaConsumerFactory<Object, Object> defaultKafkaConsumerFactory;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates and adds a kafka topic consumer to {@link KafkaMessageListenerContainer}.
     *
     * @param topic topic name to add
     */
    public void addTopicConsumer(String topic, String keyType, String valueType) {
        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setAssignmentCommitOption(ContainerProperties.AssignmentCommitOption.NEVER);
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);
        Properties properties = kafkaConfiguration.getProperties();
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, getDeserializer(keyType));
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, getDeserializer(valueType));
        containerProperties.setKafkaConsumerProperties(properties);
        KafkaMessageListenerContainer<Object, Object> kafkaMessageListenerContainer = createKafkaMessageListenerContainer(containerProperties, topic);
        kafkaMessageListenerContainer.start();
        containersMap.put(topic, kafkaMessageListenerContainer);

    }

    /**
     * Stops a listener if it's running.
     * @param topic topic name of the listener
     */
    public void stopListener(String topic) {
        if (StringUtils.isNotBlank(topic)) {
            KafkaMessageListenerContainer<Object, Object> kafkaMessageListenerContainer = containersMap.get(topic);
            if (kafkaMessageListenerContainer != null) {
                kafkaMessageListenerContainer.stop();
                log.info("Stopped listener for topic {}.", topic);
            } else {
                log.info("No listener found for topic {}.", topic);
            }
        } else {
            stopListener();
            log.info("All listeners stopped.");
        }
    }

    /**
     * Stops all the consumers of the container.
     */
    public void stopListener() {
        containersMap.forEach((s, objectObjectKafkaMessageListenerContainer) -> objectObjectKafkaMessageListenerContainer.stop());
    }

    private MongoCollection<EventEntity> getMongoCollection(String topic) {
        MongoDatabase database = mongoTemplate.getDb();
        Set<String> collectionNames = mongoTemplate.getCollectionNames();
        if (!collectionNames.contains(topic)) {
            database.createCollection(topic);
        }
        return database.getCollection(topic, EventEntity.class);
    }

    private KafkaMessageListenerContainer<Object, Object> createKafkaMessageListenerContainer(ContainerProperties containerProperties, String topic) {
        KafkaMessageListenerContainer<Object, Object> kafkaMessageListenerContainer = new KafkaMessageListenerContainer<>(defaultKafkaConsumerFactory, containerProperties);
        kafkaMessageListenerContainer.setupMessageListener((MessageListener<Object, Object>) message -> {
            log.debug("New event! Key: {}, Value: {}", message.key(), message.value());
            EventEntity eventEntity = EventEntity.fromConsumerRecord(message, objectMapper);
            getMongoCollection(topic).insertOne(eventEntity);
        });
        kafkaMessageListenerContainer.setCommonErrorHandler(new DefaultErrorHandler(
                (consumerRecord, e) -> log.error("An error occurred while consuming event.", e),
                new FixedBackOff(0L, 0L)));
        return kafkaMessageListenerContainer;
    }

    private String getDeserializer(String type) {
        return switch (type) {
            case "integer" -> IntegerDeserializer.class.getName();
            case "avro" -> ErrorHandlingDeserializer.class.getName();
            default -> StringDeserializer.class.getName();
        };
    }

}
