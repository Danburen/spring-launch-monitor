package io.github.danburen.springlaunchmonitor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ConfigSourceRecord extends LaunchRecord {
    private String sourceName;

    public ConfigSourceRecord(String sourceName, long durationMs) {
        super(System.currentTimeMillis(), durationMs);
        this.sourceName = sourceName;
    }
}
