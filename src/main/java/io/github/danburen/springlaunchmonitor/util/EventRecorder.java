package io.github.danburen.springlaunchmonitor.util;

import io.github.danburen.springlaunchmonitor.data.BeanInitRecord;
import io.github.danburen.springlaunchmonitor.data.ConfigSourceRecord;
import io.github.danburen.springlaunchmonitor.data.LaunchRecordsCtx;
import io.github.danburen.springlaunchmonitor.data.TimelineEvent;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class EventRecorder {
    // Thread safe lists to store timeline events, bean initialization records, and config source records
    private static final List<TimelineEvent> EVENTS = new CopyOnWriteArrayList<>();
    private static final List<BeanInitRecord> BEAN_RECORDS = new CopyOnWriteArrayList<>();
    private static final List<ConfigSourceRecord> CONFIG_RECORDS = new CopyOnWriteArrayList<>();

    private static volatile long applicationStartTime = 0;

    public static void setStartTime(long startTime) {
        applicationStartTime = startTime;
    }

    public static long getStartTime() {
        return applicationStartTime;
    }

    public static void recordEvent(String phase, String descKey, long durationMs, Object... args) {
        EVENTS.add(new TimelineEvent(phase, descKey, durationMs, Thread.currentThread().getName(), args));
    }

    public static void recordConfigSource(String sourceName, long durationMs) {
        CONFIG_RECORDS.add(new ConfigSourceRecord(sourceName, durationMs));
    }

    public static void recordBeanInit(String beanName, String className,
                                      long durationMs, List<String> dependencies) {
        BEAN_RECORDS.add(new BeanInitRecord(beanName, className, durationMs, dependencies));
    }

    public static void clear() {
        EVENTS.clear();
        BEAN_RECORDS.clear();
        CONFIG_RECORDS.clear();
        applicationStartTime = 0;
    }

    public static LaunchRecordsCtx getCtx(){
        return new LaunchRecordsCtx(new ArrayList<>(EVENTS), new ArrayList<>(BEAN_RECORDS), new ArrayList<>(CONFIG_RECORDS));
    }
}
