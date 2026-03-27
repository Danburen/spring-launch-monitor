package io.github.danburen.springlaunchmonitor.util;

import io.github.danburen.springlaunchmonitor.record.BeanInitRecord;
import io.github.danburen.springlaunchmonitor.record.ConfigSourceRecord;
import io.github.danburen.springlaunchmonitor.record.TimelineEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public final class StartupTimeline {
    // Thread safe lists to store timeline events, bean initialization records, and config source records
    private static final List<TimelineEvent> EVENTS = new CopyOnWriteArrayList<>();
    private static final List<BeanInitRecord> BEAN_RECORDS = new CopyOnWriteArrayList<>();
    private static final List<ConfigSourceRecord> CONFIG_RECORDS = new CopyOnWriteArrayList<>();

    private static volatile long applicationStartTime = 0;

    public static void setStartTime(long startTime) {
        applicationStartTime = startTime;
    }

    public static long getStartTime() {
        return applicationStartTime;
    }

    public static void recordEvent(String phase, String description, long durationMs) {
        EVENTS.add(new TimelineEvent(phase, description, durationMs,
                Thread.currentThread().getName()));
        log.info("[Startup] {}: {} ({}ms)", phase, description, durationMs);
    }

    public static void recordConfigSource(String sourceName, long durationMs) {
        CONFIG_RECORDS.add(new ConfigSourceRecord(sourceName, durationMs));
    }

    public static void recordBeanInit(String beanName, String className,
                                      long durationMs, List<String> dependencies) {
        BEAN_RECORDS.add(new BeanInitRecord(beanName, className, durationMs, dependencies));
    }

    public static String generateFlameGraphData() {
        StringBuilder sb = new StringBuilder();

        EVENTS.forEach(e -> sb.append(String.format("springboot;%s %d%n",
                e.getPhase().replace(";", "_"), e.getDurationMs())));

        Map<String, Long> packageTime = BEAN_RECORDS.stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            String className = r.getClassName();
                            int lastDot = className.lastIndexOf('.');
                            return lastDot > 0 ? className.substring(0, lastDot) : "default";
                        },
                        Collectors.summingLong(BeanInitRecord::getDurationMs)
                ));

        packageTime.forEach((pkg, time) ->
                sb.append(String.format("springboot;bean-init;%s %d%n",
                        pkg.replace(";", "_"), time)));

        return sb.toString();
    }

    public String generateTextReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("🚀 Spring Boot启动分析报告\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        long totalTime = EVENTS.isEmpty() ? 0
                : EVENTS.get(EVENTS.size() - 1).getDurationMs();
        sb.append(String.format("总耗时: %,dms%n%n", totalTime));

        sb.append("📊 启动阶段明细:\n");
        EVENTS.forEach(e -> {
            String bar = "█".repeat(Math.min(50, (int) e.getDurationMs() / 100));
            sb.append(String.format("  %-25s %5dms %s%n",
                    e.getPhase(), e.getDurationMs(), bar));
        });

        sb.append("\n🔥 最慢Top 10 Bean:\n");
        List<BeanInitRecord> topBeans = BEAN_RECORDS.stream()
                .sorted(Comparator.comparingLong(BeanInitRecord::getDurationMs).reversed())
                .limit(10)
                .toList();

        IntStream.range(0, topBeans.size())
                .forEach(i -> {
                    BeanInitRecord r = topBeans.get(i);
                    sb.append(String.format("  %d. %-40s %4dms%n",
                            i + 1, r.getBeanName(), r.getDurationMs()));
                });

        sb.append("\n💡 优化建议:\n");
        analyzeBottlenecks().forEach(s -> sb.append("  • ").append(s).append("\n"));

        return sb.toString();
    }

    public static List<String> analyzeBottlenecks() {
        List<String> suggestions = new ArrayList<>();

        long dbTime = BEAN_RECORDS.stream()
                .filter(r -> r.getBeanName().toLowerCase().contains("datasource")
                        || r.getBeanName().toLowerCase().contains("database"))
                .mapToLong(BeanInitRecord::getDurationMs)
                .sum();
        if (dbTime > 1000) {
            suggestions.add("数据库连接初始化耗时" + dbTime + "ms，建议检查网络或启用懒加载");
        }

        long totalBeanTime = BEAN_RECORDS.stream()
                .mapToLong(BeanInitRecord::getDurationMs)
                .sum();
        if (totalBeanTime > 3000) {
            suggestions.add("Bean初始化总耗时较长(" + totalBeanTime + "ms)，建议检查循环依赖");
        }

        long configTime = CONFIG_RECORDS.stream()
                .mapToLong(ConfigSourceRecord::getDurationMs)
                .sum();
        if (configTime > 500) {
            suggestions.add("配置加载耗时" + configTime + "ms，建议减少配置文件大小");
        }

        return suggestions.isEmpty() ? Collections.singletonList("暂无性能瓶颈") : suggestions;
    }

    public static String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("🚀 Spring Boot启动分析报告\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 总耗时
        long totalTime = EVENTS.isEmpty() ? 0
                : EVENTS.get(EVENTS.size() - 1).getDurationMs();
        sb.append(String.format("总耗时: %,dms%n%n", totalTime));

        // 阶段明细
        sb.append("📊 启动阶段明细:\n");
        for (TimelineEvent e : EVENTS) {
            int barLength = Math.min(50, (int) e.getDurationMs() / 100);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barLength; i++) bar.append("█");
            sb.append(String.format("  %-25s %5dms %s%n",
                    e.getPhase(), e.getDurationMs(), bar.toString()));
        }

        // 慢Bean Top 10
        sb.append("\n🔥 最慢Top 10 Bean:\n");
        List<BeanInitRecord> sortedBeans = new ArrayList<>(BEAN_RECORDS);
        sortedBeans.sort((r1, r2) -> Long.compare(r2.getDurationMs(), r1.getDurationMs()));

        int count = Math.min(10, sortedBeans.size());
        for (int i = 0; i < count; i++) {
            BeanInitRecord r = sortedBeans.get(i);
            sb.append(String.format("  %d. %-40s %4dms%n",
                    i + 1, r.getBeanName(), r.getDurationMs()));
        }

        // 优化建议
        sb.append("\n💡 优化建议:\n");
        List<String> suggestions = analyzeBottlenecks();
        for (String s : suggestions) {
            sb.append("  • ").append(s).append("\n");
        }

        return sb.toString();
    }

    public static List<TimelineEvent> getEvents() {
        return Collections.unmodifiableList(EVENTS);
    }

    public static List<BeanInitRecord> getBeanRecords() {
        return Collections.unmodifiableList(BEAN_RECORDS);
    }

    public static void clear() {
        EVENTS.clear();
        BEAN_RECORDS.clear();
        CONFIG_RECORDS.clear();
        applicationStartTime = 0;
    }
}
