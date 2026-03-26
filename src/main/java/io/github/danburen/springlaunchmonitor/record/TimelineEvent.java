package io.github.danburen.springlaunchmonitor.record;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimelineEvent {
    private String phase;
    private String description;
    private long durationMs;
    private String threadName;
    private long timestamp;

    public TimelineEvent(String phase, String description, long durationMs, String threadName) {
        this.phase = phase;
        this.description = description;
        this.durationMs = durationMs;
        this.threadName = threadName;
        this.timestamp = System.currentTimeMillis();
    }
}
