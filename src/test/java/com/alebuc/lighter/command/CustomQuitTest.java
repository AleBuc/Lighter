package com.alebuc.lighter.command;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomQuitTest {

    @InjectMocks
    private LighterCommand.CustomQuit customQuit;
    @Mock
    private EmbedMongoConfiguration mongoConfiguration;

    @Test
    void shouldQuit() throws InterruptedException {
        //GIVEN
        ListAppender<ILoggingEvent> logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(LighterCommand.class)).addAppender(logWatcher);
        //WHEN
        customQuit.quit();
        //THEN
        verify(mongoConfiguration).closeMongoDB();
        assertThat(logWatcher.list)
                .anySatisfy(iLoggingEvent -> {
                    assertThat(iLoggingEvent.getLevel()).isEqualTo(Level.INFO);
                    assertThat(iLoggingEvent.getFormattedMessage()).isEqualTo("Closing consumers...");
                })
                .anySatisfy(iLoggingEvent -> {
                    assertThat(iLoggingEvent.getLevel()).isEqualTo(Level.INFO);
                    assertThat(iLoggingEvent.getFormattedMessage()).isEqualTo("Closing the database...");
                });
    }

}
