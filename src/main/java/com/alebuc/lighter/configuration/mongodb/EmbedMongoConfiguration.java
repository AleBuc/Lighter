package com.alebuc.lighter.configuration.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.Objects;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class EmbedMongoConfiguration {

    private TransitionWalker.ReachedState<RunningMongodProcess> running;
    private ConnectionString connectionString;
    private final CustomCodecProvider customCodecProvider;

    /**
     * Creates and displays the connection string of the embedded MongoDB database.
     * @param version MongoDB version
     * @return the linked MongoClient
     */
    @Bean
    public MongoClient getMongoClient(Version version) {
        Transitions transitions = Mongod.instance().transitions(version)
                .replace(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.silent()).withTransitionLabel("no output"));
        running = transitions.walker().initState(StateID.of(RunningMongodProcess.class));
        connectionString = createConnectionString(running.current().getServerAddress());
        log.info("Connection string: {}", connectionString);
        log.info("Use 'help [command]' to display commands or the function details.");
        log.info("Use 'exit' or 'quit' to correctly close the application.\n");
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(customCodecProvider));
        MongoClientSettings settings = MongoClientSettings.builder().codecRegistry(codecRegistry).applyConnectionString(connectionString).build();
        return MongoClients.create(settings);
    }

    /**
     * Gets the MongoDB version.
     * @return the used MongoDB version
     */
    @Bean
    public Version getVersion() {
        return Version.V7_0_2;
    }

    /**
     * Initializes MongoDatabaseFactory.
     * @param mongoClient {@link MongoClient} linked to the database
     * @return a new {@link MongoDatabaseFactory}
     */
    @Bean
    public MongoDatabaseFactory getMongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "Lighter");
    }

    /**
     * Creates the MongoTemplate.
     * @param mongoDatabaseFactory the {@link MongoDatabaseFactory} linked to the embedded database
     * @return the new {@link MongoTemplate}
     */
    @Bean
    public MongoTemplate getMongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

    /**
     * Closes the embedded MongoDB database if it is running.
     */
    public void closeMongoDB() {
        if (!Objects.isNull(running) && running.current().isAlive()) {
            running.close();
        }
    }

    private ConnectionString createConnectionString(ServerAddress serverAddress) {
        return new ConnectionString(String.format("mongodb://%s:%s", serverAddress.getHost(), serverAddress.getPort()));
    }

}
