package com.alebuc.lighter.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alebuc.lighter.configuration.KafkaConfiguration;
import com.alebuc.lighter.utils.JsonFormatter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    @InjectMocks
    private KafkaService kafkaService;
    @Mock
    private KafkaConfiguration kafkaConfiguration;

    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeEach
    void init() {
        this.logWatcher = new ListAppender<>();
        this.logWatcher.start();
        ((Logger) LoggerFactory.getLogger(KafkaService.class)).addAppender(this.logWatcher);
    }

    @Test
    void shouldConsumeTopic() throws InterruptedException {
        //GIVEN
        String testTopic = "testTopic";
        KafkaConsumer<String, Object> kafkaConsumer = Mockito.mock(KafkaConsumer.class);
        when(kafkaConfiguration.getConsumer()).thenReturn(kafkaConsumer);
        String key1 = "key1";
        Map<String, Object> testValueObject1 = new LinkedHashMap<>();
        testValueObject1.put("id", "1234");
        testValueObject1.put("count", 10);
        String key2 = "key2";
        String testValueObject2 = "value2";
        ConsumerRecords<String, Object> consumerRecords = new ConsumerRecords<>(Map.of(
                new TopicPartition(testTopic, 0), List.of(new ConsumerRecord<>(testTopic, 0, 0, key1, JsonFormatter.parseObjectToJson(testValueObject1))),
                new TopicPartition(testTopic, 1), List.of(new ConsumerRecord<>(testTopic, 1, 0, key2, testValueObject2))
        ));
        when(kafkaConsumer.poll(Duration.ofMillis(100))).thenReturn(consumerRecords).then(invocation -> {
            Thread.sleep(100);
            return ConsumerRecords.empty();
        });

        //WHEN
        Thread consumptionThread = new Thread(() -> kafkaService.consumeTopic(testTopic));
        Thread stopThread = new Thread(kafkaService::stopListener);
        consumptionThread.start();
        Thread.sleep(100);
        stopThread.start();

        //THEN
        List<ILoggingEvent> logs = logWatcher.list;
        assertThat(logs)
                .anySatisfy(iLoggingEvent -> {
                    assertThat(iLoggingEvent.getLevel()).isEqualTo(Level.INFO);
                    assertThat(iLoggingEvent.getFormattedMessage()).isEqualTo("New event! Key: key1, Value: {\"id\":\"1234\",\"count\":10}");
                })
                .anySatisfy(iLoggingEvent -> {
                    assertThat(iLoggingEvent.getLevel()).isEqualTo(Level.INFO);
                    assertThat(iLoggingEvent.getFormattedMessage()).isEqualTo("New event! Key: key2, Value: value2");
                });
    }

}
