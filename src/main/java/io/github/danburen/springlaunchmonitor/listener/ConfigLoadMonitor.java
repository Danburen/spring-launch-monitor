package io.github.danburen.springlaunchmonitor.listener;

import io.github.danburen.springlaunchmonitor.util.StartupTimeline;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.concurrent.TimeUnit;

public class ConfigLoadMonitor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        long start = System.nanoTime();

        for (PropertySource<?> source : environment.getPropertySources()) {
            long sourceStart = System.nanoTime();
            source.getProperty("spring.application.name");
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - sourceStart);

            StartupTimeline.recordConfigSource(source.getName(), duration);
        }
    }
}
