package com.alebuc.lighter;

import com.alebuc.lighter.configuration.EmbedMongoConfiguration;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@CommandLine.Command(name = "lighter", mixinStandardHelpOptions = true,
        description = "Run an throwable embedded MongoDB database.", versionProvider = Lighter.PropertiesVersionProvider.class)
public class Lighter implements Callable<String> {
    private static final Logger log = Logger.getGlobal();

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true, description = "Print Lighter version.")
    boolean versionDisplay;

    public String call() {
        CommandLine.usage(this, System.out);
        log.info("Run database.");
        EmbedMongoConfiguration configuration = EmbedMongoConfiguration.getInstance();
        configuration.startMongoDB();
        log.info("------ Connection string: " + configuration.getConnectionString() + " ------");
        try {
            try {
                System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } finally {
            log.info("Closing database.");
            configuration.closeMongoDB();
        }
        log.info("Stopping the app.");
        return null;
    }

    public static void main(String[] args) {
        new CommandLine(new Lighter()).execute(args);
    }

    static class PropertiesVersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() throws IOException {
            final Properties properties = new Properties();
            InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");
            properties.load(inputStream);
            return new String[]{properties.getProperty("project.version")};
        }
    }
}
