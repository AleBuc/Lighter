package com.alebuc.lighter.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alebuc.lighter.utils.JsonFormatter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class KafkaServiceTest {

    private ListAppender<ILoggingEvent> logWatcher;
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"));

    String topicName = "testTopic";
    String bootstrapServers;
    KafkaProducer<String, String> producer;

    @BeforeEach
    void init() {
        this.logWatcher = new ListAppender<>();
        this.logWatcher.start();
        ((Logger) LoggerFactory.getLogger(KafkaService.class)).addAppender(this.logWatcher);
        kafka.start();
        bootstrapServers = kafka.getBootstrapServers();
        producer = new KafkaProducer<>(
                ImmutableMap.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.CLIENT_ID_CONFIG, "producer-test"
                ),
                new StringSerializer(),
                new StringSerializer()
        );
    }

    @AfterEach
    void close() {
        kafka.stop();
    }

    @Test
    void shouldConsumeTopic() throws InterruptedException {
        //GIVEN
        String testTopic = "testTopic";

        String key1 = "key1";
        Map<String, Object> testValueObject1 = new LinkedHashMap<>();
        testValueObject1.put("id", "1234");
        testValueObject1.put("count", 10);
        String key2 = "key2";
        String testValueObject2 = "value2";
        producer.send(new ProducerRecord<>(topicName, key1, JsonFormatter.parseObjectToJson(testValueObject1)));
        producer.send(new ProducerRecord<>(topicName, key2, testValueObject2));

        //WHEN
        KafkaService kafkaService = KafkaService.INSTANCE;
        Thread consumptionThread = new Thread(() -> kafkaService.consumeTopic(bootstrapServers, testTopic));
        Thread stopThread = new Thread(kafkaService::stopListener);
        consumptionThread.start();
        Thread.sleep(1000);
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
