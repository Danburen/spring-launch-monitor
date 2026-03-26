package io.github.danburen.springlaunchmonitor.listener;

import io.github.danburen.springlaunchmonitor.util.StartupTimeline;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.TimeUnit;

public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        long duration = TimeUnit.NANOSECONDS.toMillis(
                System.nanoTime() - StartupTimeline.getStartTime());
        StartupTimeline.recordEvent("ContextRefreshed", "上下文刷新完成", duration);
    }
}