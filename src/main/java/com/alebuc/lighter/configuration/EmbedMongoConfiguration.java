package com.alebuc.lighter.configuration;

import com.mongodb.ConnectionString;
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

    @Bean
    public MongoClient getMongoClient() {
        Transitions transitions = Mongod.instance().transitions(Version.Main.V7_0)
                .replace(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.silent()).withTransitionLabel("no output"));
        running = transitions.walker().initState(StateID.of(RunningMongodProcess.class));
        connectionString = createConnectionString(running.current().getServerAddress());
        log.info("Connection string: {}", connectionString);
        return MongoClients.create(connectionString);
    }

    @Bean
    public MongoDatabaseFactory getMongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "Lighter");
    }


    @Bean
    public MongoTemplate getMongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

    public void closeMongoDB() {
        if (!Objects.isNull(running) && running.current().isAlive()) {
            running.close();
        }
    }

    private ConnectionString createConnectionString(ServerAddress serverAddress) {
        return new ConnectionString(String.format("mongodb://%s:%s", serverAddress.getHost(), serverAddress.getPort()));
    }

}
