package com.alebuc.lighter.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import lombok.Getter;

import java.io.IOException;

public class EmbedMongoConfiguration {

    private EmbedMongoConfiguration(){}

    private static final class InstanceHolder {

        private static final EmbedMongoConfiguration instance = new EmbedMongoConfiguration();
    }
    public static EmbedMongoConfiguration getInstance() {
        return InstanceHolder.instance;
    }

    @Getter
    private TransitionWalker.ReachedState<RunningMongodProcess> running;
    @Getter
    private ConnectionString connectionString;

    public void startMongoDB() throws IOException {
        Transitions transitions = Mongod.instance().transitions(Version.Main.V4_4);
        running = transitions.walker().initState(StateID.of(RunningMongodProcess.class));
        try {
            connectionString = createConnectionString(running.current().getServerAddress());
            try (MongoClient mongoClient = MongoClients.create(connectionString)) {
                System.out.printf("Connection string: %s%n", connectionString);
                System.in.read();

            }
        } finally {
            running.close();
        }
    }

    private ConnectionString createConnectionString(ServerAddress serverAddress) {
        return new ConnectionString(String.format("mongodb://%s:%s", serverAddress.getHost(), serverAddress.getPort()));
    }

}
