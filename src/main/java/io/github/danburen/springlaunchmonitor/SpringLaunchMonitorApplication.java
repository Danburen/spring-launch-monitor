package io.github.danburen.springlaunchmonitor;

import io.github.danburen.springlaunchmonitor.util.StartupTimeline;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringLaunchMonitorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringLaunchMonitorApplication.class);
        app.run(args);
    }
}
