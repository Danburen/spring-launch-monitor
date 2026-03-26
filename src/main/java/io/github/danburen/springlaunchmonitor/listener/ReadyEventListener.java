package io.github.danburen.springlaunchmonitor.listener;

import io.github.danburen.springlaunchmonitor.util.StartupTimeline;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.TimeUnit;

public class ReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        long duration = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - StartupTimeline.getStartTime());
        StartupTimeline.recordEvent("ApplicationReady", "应用就绪", duration);

        String report = StartupTimeline.generateReport();
        System.out.println(report);
        String heat = StartupTimeline.generateFlameGraphData();
        System.out.println(heat);
    }
}