package io.github.danburen.springlaunchmonitor.spring.listener;

import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.TimeUnit;

public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        long duration = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - EventRecorder.getStartTime());
        EventRecorder.recordEvent("ContextRefreshed", "phase.context.refreshed", duration);
    }
}