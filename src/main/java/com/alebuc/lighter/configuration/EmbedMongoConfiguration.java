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

import java.io.IOException;

public class EmbedMongoConfiguration {


    public void startMongoDB() throws IOException {
        Transitions transitions = Mongod.instance().transitions(Version.Main.V4_4);
        try (TransitionWalker.ReachedState<RunningMongodProcess> running = transitions.walker().initState(StateID.of(RunningMongodProcess.class))) {
            ConnectionString connectionString = createConnectionString(running.current().getServerAddress());
            try (MongoClient mongoClient = MongoClients.create(connectionString)) {
                System.out.printf("Connection string: %s%n", connectionString);
                System.in.read();

            }
        }
    }

    private ConnectionString createConnectionString(ServerAddress serverAddress) {
        return new ConnectionString(String.format("mongodb://%s:%s", serverAddress.getHost(), serverAddress.getPort()));
    }
}
