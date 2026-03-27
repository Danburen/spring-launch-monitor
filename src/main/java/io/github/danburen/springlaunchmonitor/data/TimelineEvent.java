package io.github.danburen.springlaunchmonitor.record;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimelineEvent {
    private String phase;
    private String desckey;
    private long durationMs;
    private String threadName;
    private long timestamp;
    private Object [] args;

    public TimelineEvent(String phase, String descKey, long durationMs, String threadName, Object... args) {
        this.phase = phase;
        this.desckey = descKey;
        this.durationMs = durationMs;
        this.threadName = threadName;
        this.timestamp = System.currentTimeMillis();
        this.args = args;
    }

    public TimelineEvent(String phase, String descKey, long durationMs, String threadName) {
        this.phase = phase;
        this.desckey = descKey;
        this.durationMs = durationMs;
        this.threadName = threadName;
        this.timestamp = System.currentTimeMillis();
    }
}
