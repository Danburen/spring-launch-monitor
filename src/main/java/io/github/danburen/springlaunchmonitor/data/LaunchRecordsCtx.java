package io.github.danburen.springlaunchmonitor.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LaunchRecordsCtx {
    private List<TimelineEvent> events;
    private List<BeanInitRecord> beanRecords;
    private List<ConfigSourceRecord> configRecords;
}
