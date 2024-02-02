package com.alebuc.lighter;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Callable;

@Slf4j
@CommandLine.Command(name = "lighter", subcommands = {Runner.class},
        description = "Run an throwable embedded MongoDB database.", versionProvider = Lighter.PropertiesVersionProvider.class)
public class Lighter implements Callable<String> {

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true, description = "Print Lighter version.")
    boolean versionDisplay;

    @CommandLine.Option(names = {"-h", "--help"},
            usageHelp = true,
            description = "Display this help message.")
    boolean usageHelpRequested;

    public String call() {
        return null;
    }

    public static void main(String[] args) {
        if (args != null && args.length >0){
            PicocliRunner.call(Lighter.class, args);
        } else {
            PicocliRunner.call(Lighter.class,"--help");
        }
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
