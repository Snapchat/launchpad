package com.snapchat.launchpad;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class LaunchpadApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaunchpadApplication.class, args);
    }
}
