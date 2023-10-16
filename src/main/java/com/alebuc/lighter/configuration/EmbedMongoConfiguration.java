package com.alebuc.lighter.configuration;

import com.mongodb.ConnectionString;
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

import java.util.Objects;

public class EmbedMongoConfiguration {

    private EmbedMongoConfiguration() {
    }

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

    public void startMongoDB() {
        Transitions transitions = Mongod.instance().transitions(Version.Main.V4_4)
                .replace(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.silent()).withTransitionLabel("no output"));
        running = transitions.walker().initState(StateID.of(RunningMongodProcess.class));
        connectionString = createConnectionString(running.current().getServerAddress());
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
