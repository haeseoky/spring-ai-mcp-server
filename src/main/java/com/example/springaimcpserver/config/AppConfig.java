package com.example.springaimcpserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.concurrent.Executor;

@Configuration
public class AppConfig {

    @Value("${app.document.temp-dir}")
    private String tempDir;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("DocumentGen-");
        executor.initialize();
        return executor;
    }

    @Bean
    public void initTempDirectory() {
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
