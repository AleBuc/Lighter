package com.alebuc.lighter.service;

import com.alebuc.lighter.configuration.KafkaConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {

    @Getter
    private boolean isListening = false;
    private final KafkaConfiguration kafkaConfiguration;
    private final Map<String, KafkaConsumer<Object, Object>> consumerMap = new HashMap<>();
    private final List<KafkaMessageListenerContainer<Object,Object>> containers = new ArrayList<>();
    private final DefaultKafkaConsumerFactory<Object,Object> defaultKafkaConsumerFactory;

    public void addTopicConsumer(String topic) {
        ContainerProperties containerProperties = new ContainerProperties(topic);
        KafkaMessageListenerContainer<Object, Object> kafkaMessageListenerContainer = new KafkaMessageListenerContainer<>(defaultKafkaConsumerFactory, containerProperties);
        BlockingQueue<ConsumerRecord<Object,Object>> records = new LinkedBlockingQueue<>();
        kafkaMessageListenerContainer.setupMessageListener((MessageListener<Object,Object>) message -> {
            log.info("New event! Key: {}, Value: {}", message.key(), message.value());
            records.add(message);
        });
        kafkaMessageListenerContainer.start();
        containers.add(kafkaMessageListenerContainer);

    }

    public void stopListener() {
        containers.forEach(AbstractMessageListenerContainer::stop);
    }
}
