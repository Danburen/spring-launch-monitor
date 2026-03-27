package io.github.danburen.springlaunchmonitor.listener;

import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.TimeUnit;

public class ReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        long duration = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - EventRecorder.getStartTime());
        EventRecorder.recordEvent("ApplicationReady", "phase.application.ready", duration);

        String report = EventRecorder.generateReport();
        System.out.println(report);
        String heat = EventRecorder.generateFlameGraphData();
        System.out.println(heat);
    }
}