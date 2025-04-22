package ru.kata.project.bitbucketconfigserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class BitbucketConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BitbucketConfigServerApplication.class, args);
    }

}
