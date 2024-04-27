package com.alebuc.lighter;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
public class Lighter {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lighter.class);
        System.setProperty("spring.config.name","config");
        application.setBannerMode(Banner.Mode.CONSOLE);
        application.run(args);
    }
}
