package com.enigmabridge.log.distributor;

/**
 * Created by dusanklinec on 20.07.16.
 */
import com.enigmabridge.log.distributor.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static final String ROUTER_RELOAD_EXECUTOR = "reloadExecutor";
    public static final String SERVER_RESYNC_EXECUTOR = "resyncExecutor";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Started...");
    }

    @Bean(name = ROUTER_RELOAD_EXECUTOR)
    public Executor reloadExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("router-reload-exec"));
    }

    @Bean(name = SERVER_RESYNC_EXECUTOR)
    public Executor resyncExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("server-resync-exec"));
    }
}
