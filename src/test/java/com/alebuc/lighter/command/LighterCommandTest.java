package com.alebuc.lighter.command;

import com.alebuc.lighter.service.KafkaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LighterCommandTest {

    @InjectMocks
    private LighterCommand lighterCommand;
    @Mock
    private KafkaService kafkaService;

    private static final String TOPIC_NAME = "testTopic";

    @Test
    void shouldConsumeTopic() {
        //WHEN
        lighterCommand.consume(TOPIC_NAME, null, null);
        //THEN
        Mockito.verify(kafkaService).addTopicConsumer(TOPIC_NAME, null, null);
    }
}