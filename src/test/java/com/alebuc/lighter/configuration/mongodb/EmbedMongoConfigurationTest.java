package com.alebuc.lighter.configuration.mongodb;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbedMongoConfigurationTest {

    @InjectMocks
    private EmbedMongoConfiguration embedMongoConfiguration;
    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeEach
    void init() {
        this.logWatcher = new ListAppender<>();
        this.logWatcher.start();
        ((Logger) LoggerFactory.getLogger(EmbedMongoConfiguration.class)).addAppender(this.logWatcher);
    }

    @Test
    void shouldGetMongoClient() {
        //WHEN
        MongoClient mongoClient = embedMongoConfiguration.getMongoClient(Version.V7_0_2);
        ConnectionString connectionString = embedMongoConfiguration.getConnectionString();
        embedMongoConfiguration.closeMongoDB();
        //THEN
        assertThat(mongoClient)
                .isNotNull();
        assertThat(connectionString)
                .isNotNull();
        assertThat(logWatcher.list)
                .anySatisfy(iLoggingEvent -> {
                    assertThat(iLoggingEvent.getLevel()).isEqualTo(Level.INFO);
                    assertThat(iLoggingEvent.getFormattedMessage()).isEqualTo(String.format("Connection string: %s", connectionString));
                });
    }

    @Test
    void shouldGetMongoDatabaseFactory() {
        //GIVEN
        MongoClient mongoClient = Mockito.mock(MongoClient.class);
        //WHEN
        MongoDatabaseFactory mongoDatabaseFactory = embedMongoConfiguration.getMongoDatabaseFactory(mongoClient);
        //THEN
        assertThat(mongoDatabaseFactory)
                .isNotNull();
    }

    @Test
    void shouldGetMongoTemplate() {
        //GIVEN
        MongoDatabaseFactory mongoDatabaseFactory = Mockito.mock(MongoDatabaseFactory.class);
        when(mongoDatabaseFactory.getExceptionTranslator()).thenReturn(new MongoExceptionTranslator());
        //WHEN
        MongoTemplate mongoTemplate = embedMongoConfiguration.getMongoTemplate(mongoDatabaseFactory);
        //THEN
        assertThat(mongoTemplate)
                .isNotNull();
    }

}
