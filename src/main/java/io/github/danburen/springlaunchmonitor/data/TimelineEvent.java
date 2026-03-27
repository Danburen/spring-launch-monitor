package io.github.danburen.springlaunchmonitor.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TimelineEvent extends LaunchRecord{
    private String phase;
    private String desckey;
    private String threadName;
    private Object [] args;

    public TimelineEvent(String phase, String descKey, long durationMs, String threadName, Object... args) {
        super(System.currentTimeMillis(), durationMs);
        this.phase = phase;
        this.desckey = descKey;
        this.threadName = threadName;
        this.args = args;
    }

}
