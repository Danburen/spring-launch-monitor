package io.github.danburen.springlaunchmonitor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LaunchRecord {
    private long timestamp;
    private long durationMs;
}
