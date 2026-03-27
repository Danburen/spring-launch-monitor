package io.github.danburen.springlaunchmonitor.record;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BeanInitRecord {
    private String beanName;
    private String className;
    private long durationMs;
    private List<String> dependencies;
    private long timestamp;

    public BeanInitRecord(String beanName, String className, long durationMs,
                          List<String> dependencies) {
        this.beanName = beanName;
        this.className = className;
        this.durationMs = durationMs;
        this.dependencies = dependencies;
        this.timestamp = System.currentTimeMillis();
    }
}
