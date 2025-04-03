package com.example.springaimcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringAiMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiMcpServerApplication.class, args);
    }

}
