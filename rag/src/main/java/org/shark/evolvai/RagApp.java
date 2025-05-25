package org.shark.evolvai;

import org.shark.evolvai.config.RagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ RagProperties.class})

// @EnableAspectJAutoProxy
public class RagApp {
    public static void main(String[] args) {
        SpringApplication.run(RagApp.class, args);
    }
}