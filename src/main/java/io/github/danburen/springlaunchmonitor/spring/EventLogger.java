package io.github.danburen.springlaunchmonitor.component;

import io.github.danburen.springlaunchmonitor.data.BeanInitRecord;
import io.github.danburen.springlaunchmonitor.data.ConfigSourceRecord;
import io.github.danburen.springlaunchmonitor.data.TimelineEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class EventLogger implements ApplicationListener<ApplicationReadyEvent> , Ordered {
    private Locale locale;
    private Environment environment;

    @PostConstruct
    public void init(){
        this.locale = Locale.of(
                environment.getProperty("launch.monitor.locale",
                        Locale.getDefault().toString())
        );
    }



    private String getMessage(String code, String defaultMessage, Object... args) {
        return "";
    }

    private String getMessage(String code, String defaultMessage, Locale locale) {
        return getMessage(code, defaultMessage, locale, null);
    }

    public static String generateFlameGraphData() {
        StringBuilder sb = new StringBuilder();

        events.forEach(e -> sb.append(String.format("springboot;%s %d%n",
                e.getPhase().replace(";", "_"), e.getDurationMs())));

        Map<String, Long> packageTime = beanRecords.stream()
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

    public String generateTextReport(List<TimelineEvent> events,  List<BeanInitRecord> beanRecords, List<ConfigSourceRecord> configRecords) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚀 Spring Boot启动分析报告\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        long totalTime = events.isEmpty() ? 0
                : events.get(events.size() - 1).getDurationMs();
        sb.append(String.format("总耗时: %,dms%n%n", totalTime));

        sb.append("📊 启动阶段明细:\n");
        events.forEach(e -> {
            String bar = "█".repeat(Math.min(50, (int) e.getDurationMs() / 100));
            sb.append(String.format("  %-25s %5dms %s%n",
                    e.getPhase(), e.getDurationMs(), bar));
        });

        sb.append("\n🔥 最慢Top 10 Bean:\n");
        List<BeanInitRecord> topBeans = beanRecords.stream()
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
        analyzeBottlenecks(events, beanRecords, configRecords).forEach(s -> sb.append("  • ").append(s).append("\n"));

        return sb.toString();
    }

    public static List<String> analyzeBottlenecks(List<TimelineEvent> events,  List<BeanInitRecord> beanRecords, List<ConfigSourceRecord> configRecords) {
        List<String> suggestions = new ArrayList<>();

        long dbTime = beanRecords.stream()
                .filter(r -> r.getBeanName().toLowerCase().contains("datasource")
                        || r.getBeanName().toLowerCase().contains("database"))
                .mapToLong(BeanInitRecord::getDurationMs)
                .sum();
        if (dbTime > 1000) {
            suggestions.add("数据库连接初始化耗时" + dbTime + "ms，建议检查网络或启用懒加载");
        }

        long totalBeanTime = beanRecords.stream()
                .mapToLong(BeanInitRecord::getDurationMs)
                .sum();
        if (totalBeanTime > 3000) {
            suggestions.add("Bean初始化总耗时较长(" + totalBeanTime + "ms)，建议检查循环依赖");
        }

        long configTime = configRecords.stream()
                .mapToLong(ConfigSourceRecord::getDurationMs)
                .sum();
        if (configTime > 500) {
            suggestions.add("配置加载耗时" + configTime + "ms，建议减少配置文件大小");
        }

        return suggestions.isEmpty() ? Collections.singletonList("暂无性能瓶颈") : suggestions;
    }

    public static String generateReport(List<TimelineEvent> events,  List<BeanInitRecord> beanRecords, List<ConfigSourceRecord> configRecords) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("🚀 Spring Boot启动分析报告\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 总耗时
        long totalTime = events.isEmpty() ? 0
                : events.get(events.size() - 1).getDurationMs();
        sb.append(String.format("总耗时: %,dms%n%n", totalTime));

        // 阶段明细
        sb.append("📊 启动阶段明细:\n");
        for (TimelineEvent e : events) {
            int barLength = Math.min(50, (int) e.getDurationMs() / 100);
            StringBuilder bar = new StringBuilder();
            bar.append("█".repeat(Math.max(0, barLength)));
            sb.append(String.format("  %-25s %5dms %s%n",
                    e.getPhase(), e.getDurationMs(), bar.toString()));
        }

        // 慢Bean Top 10
        sb.append("\n🔥 最慢Top 10 Bean:\n");
        List<BeanInitRecord> sortedBeans = new ArrayList<>(beanRecords);
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

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

    }
}
