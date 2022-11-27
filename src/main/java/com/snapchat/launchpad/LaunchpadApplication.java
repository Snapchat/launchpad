package com.snapchat.launchpad;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class LaunchpadApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaunchpadApplication.class, args);
    }
}
