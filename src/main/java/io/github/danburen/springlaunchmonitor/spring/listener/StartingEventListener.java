package io.github.danburen.springlaunchmonitor.spring.listener;

import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

public class StartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        long startTime = System.nanoTime();
        EventRecorder.setStartTime(startTime);
        EventRecorder.recordEvent("ApplicationStarting", "phase.application.starting", 0);
    }
}