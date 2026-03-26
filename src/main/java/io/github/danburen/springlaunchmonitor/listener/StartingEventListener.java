package io.github.danburen.springlaunchmonitor.listener;

import io.github.danburen.springlaunchmonitor.util.StartupTimeline;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.TimeUnit;

public class StartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        long startTime = System.nanoTime();
        StartupTimeline.setStartTime(startTime);
        StartupTimeline.recordEvent("ApplicationStarting", "启动开始", 0);
        System.out.println("[StartupMonitor] 启动监控开始");
    }
}