package io.github.danburen.springlaunchmonitor.spring.flame;

import io.github.danburen.springlaunchmonitor.data.BeanInitRecord;
import io.github.danburen.springlaunchmonitor.data.ConfigSourceRecord;
import io.github.danburen.springlaunchmonitor.data.LaunchRecordsCtx;
import io.github.danburen.springlaunchmonitor.data.TimelineEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageTreeFlameGraphDataGeneratorTests {

    @Test
    void shouldGenerateFoldedDataAndPackageTree() {
        FlameGraphDataGenerator generator = new PackageTreeFlameGraphDataGenerator();

        List<TimelineEvent> events = List.of(
                new TimelineEvent("ApplicationStarting", "phase.application.starting", 0, "main"),
                new TimelineEvent("ApplicationReady", "phase.application.ready", 1500, "main")
        );

        List<BeanInitRecord> beans = List.of(
                new BeanInitRecord("a", "org.springframework.context.A", 100, List.of()),
                new BeanInitRecord("b", "org.springframework.boot.B", 200, List.of()),
                new BeanInitRecord("c", "io.github.demo.C", 50, List.of())
        );

        LaunchRecordsCtx ctx = new LaunchRecordsCtx(events, beans, List.<ConfigSourceRecord>of());
        String output = generator.generate(ctx);

        assertTrue(output.contains("springboot;ApplicationReady 1500"));
        assertTrue(output.contains("springboot;bean-init;org 300"));
        assertTrue(output.contains("springboot;bean-init;org;springframework 100"));
        assertTrue(output.contains("- org (total: 300ms"));
        assertTrue(output.contains("- springframework (total: 100ms"));
    }
}

