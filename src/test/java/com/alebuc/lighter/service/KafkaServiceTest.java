package com.alebuc.lighter.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alebuc.lighter.configuration.kafka.KafkaConfiguration;
import com.alebuc.lighter.configuration.kafka.KafkaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaServiceTest {

    private ListAppender<ILoggingEvent> logWatcher;
    @Mock
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void init() {
        this.logWatcher = new ListAppender<>();
        this.logWatcher.start();
        ((Logger) LoggerFactory.getLogger(KafkaService.class)).addAppender(this.logWatcher);
    }

    @Test
    void shouldAddTopicConsumer()  {
        //GIVEN
            String testTopic = "testTopic";
            KafkaProperties.ConnectionProperties serverProperties = new KafkaProperties.ConnectionProperties();
            serverProperties.setAddress("http://localhost:29092");
            KafkaProperties kafkaProperties = new KafkaProperties();
            kafkaProperties.setServer(serverProperties);
            KafkaConfiguration kafkaConfiguration = new KafkaConfiguration(kafkaProperties);
            KafkaService kafkaService = new KafkaService(kafkaConfiguration, kafkaConfiguration.getKafkaConsumerFactory(), mongoTemplate);

            //WHEN
            kafkaService.addTopicConsumer(testTopic, "string", "string");
            //THEN
            assertThat(kafkaService.getContainersMap())
                    .hasSize(1);
    }

    @Test
    void shouldStopConsumers() {
        //GIVEN
        KafkaMessageListenerContainer<Object,Object> kafkaMessageListenerContainer = Mockito.mock(KafkaMessageListenerContainer.class);
        Map<String, KafkaMessageListenerContainer<Object,Object>> kafkaMessageListenerContainers = new HashMap<>();
        kafkaMessageListenerContainers.put("topic", kafkaMessageListenerContainer);
        KafkaService kafkaService = new KafkaService(mock(KafkaConfiguration.class), mock(DefaultKafkaConsumerFactory.class), mongoTemplate);
        ReflectionTestUtils.setField(kafkaService, "containersMap", kafkaMessageListenerContainers);
        //WHEN
        kafkaService.stopListener();
        //THEN
        verify(kafkaMessageListenerContainer).stop();
    }

    @Test
    void shouldStopConsumer() {
        //GIVEN
        KafkaMessageListenerContainer<Object,Object> kafkaMessageListenerContainer1 = Mockito.mock(KafkaMessageListenerContainer.class);
        KafkaMessageListenerContainer<Object,Object> kafkaMessageListenerContainer2 = Mockito.mock(KafkaMessageListenerContainer.class);
        Map<String, KafkaMessageListenerContainer<Object,Object>> kafkaMessageListenerContainers = new HashMap<>();
        kafkaMessageListenerContainers.put("topic1", kafkaMessageListenerContainer1);
        kafkaMessageListenerContainers.put("topic2", kafkaMessageListenerContainer2);
        KafkaService kafkaService = new KafkaService(mock(KafkaConfiguration.class), mock(DefaultKafkaConsumerFactory.class), mongoTemplate);
        ReflectionTestUtils.setField(kafkaService, "containersMap", kafkaMessageListenerContainers);
        //WHEN
        kafkaService.stopListener("topic1");
        //THEN
        verify(kafkaMessageListenerContainer1).stop();
        verify(kafkaMessageListenerContainer2, never()).stop();
    }

}
