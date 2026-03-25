package mj.mycrawler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorConfig {

    @Value("${crawler.schedulerDelayMs}")
    private long schedulerDelayMs;

    @Value("${crawler.workerPoolSize}")
    private int workerPoolSize;

    @Bean(name = "scheduledExecutorService", destroyMethod = "shutdown")
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "crawler-scheduler");
            t.setDaemon(false);
            return t;
        });
    }

    @Bean(name = "workerExecutorService", destroyMethod = "shutdown")
    public ExecutorService workerExecutorService() {
        return Executors.newFixedThreadPool(workerPoolSize, r -> {
            Thread t = new Thread(r);
            t.setName("crawler-worker-" + t.getId());
            t.setDaemon(false);
            return t;
        });
    }

    @Bean
    public Long schedulerDelayMs() {
        return schedulerDelayMs;
    }
}