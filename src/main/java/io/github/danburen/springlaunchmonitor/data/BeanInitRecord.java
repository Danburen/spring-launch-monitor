package io.github.danburen.springlaunchmonitor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;



@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class BeanInitRecord extends LaunchRecord {
    private String beanName;
    private String className;
    private List<String> dependencies;

    public BeanInitRecord(String beanName, String className, long durationMs,
                          List<String> dependencies) {
        super(System.currentTimeMillis(), durationMs);
        this.beanName = beanName;
        this.className = className;
        this.dependencies = dependencies;
    }
}
