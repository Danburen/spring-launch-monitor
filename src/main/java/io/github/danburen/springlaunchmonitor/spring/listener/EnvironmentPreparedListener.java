package io.github.danburen.springlaunchmonitor.spring.listener;

import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertySource;

import java.util.concurrent.TimeUnit;

public class EnvironmentPreparedListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        long duration = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - EventRecorder.getStartTime());
        EventRecorder.recordEvent("EnvironmentPrepared", "phase.environment.prepared", duration);

        // Probe each property source once to estimate config access overhead.
        for (PropertySource<?> source : event.getEnvironment().getPropertySources()) {
            long sourceStart = System.nanoTime();
            source.getProperty("spring.application.name");
            long sourceDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - sourceStart);
            EventRecorder.recordConfigSource(source.getName(), sourceDuration);
        }
    }
}