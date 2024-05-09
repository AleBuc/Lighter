package com.alebuc.lighter;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

/**
 * Lighter application main class
 */
@SpringBootApplication
@CommandScan
public class Lighter {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lighter.class);
        System.setProperty("spring.config.name","config");
        System.setProperty("spring.main.allow-bean-definition-overriding","true");
        application.setAdditionalProfiles("default");
        application.setLogStartupInfo(false);
        application.setBannerMode(Banner.Mode.CONSOLE);
        try {
            application.run(args);
        } catch (Exception e) {
            //NONE
        }
    }
}
