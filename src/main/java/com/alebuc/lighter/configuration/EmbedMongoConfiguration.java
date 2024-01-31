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
import jakarta.inject.Singleton;
import lombok.Getter;

import java.util.Objects;

@Getter
@Singleton
public class EmbedMongoConfiguration {

    private TransitionWalker.ReachedState<RunningMongodProcess> running;

    private ConnectionString connectionString;

    public void startMongoDB() {
        Transitions transitions = Mongod.instance().transitions(Version.Main.V6_0)
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
