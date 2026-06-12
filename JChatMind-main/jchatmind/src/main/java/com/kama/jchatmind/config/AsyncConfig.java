package com.kama.jchatmind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/*
 * ??????? -- ????(?????)????????,??????
 * @EnableAsync:??????,? @Async ????
 * corePoolSize=4:???????;maxPoolSize=10:?????;queueCapacity=100:??????
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);       // ??4???
        executor.setMaxPoolSize(10);       // ?????10???
        executor.setQueueCapacity(100);    // 100???????
        executor.setThreadNamePrefix("async-event-");  // ?????
        executor.initialize();
        return executor;
    }
}