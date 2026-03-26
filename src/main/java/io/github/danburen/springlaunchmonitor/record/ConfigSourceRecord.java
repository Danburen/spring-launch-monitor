package io.github.danburen.springlaunchmonitor.record;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigSourceRecord {
    private String sourceName;
    private long durationMs;
    private long timestamp;

    public ConfigSourceRecord(String sourceName, long durationMs) {
        this.sourceName = sourceName;
        this.durationMs = durationMs;
        this.timestamp = System.currentTimeMillis();
    }
}
