package io.github.danburen.springlaunchmonitor.spring;

import io.github.danburen.springlaunchmonitor.data.BeanInitRecord;
import io.github.danburen.springlaunchmonitor.data.ConfigSourceRecord;
import io.github.danburen.springlaunchmonitor.data.LaunchRecordsCtx;
import io.github.danburen.springlaunchmonitor.data.TimelineEvent;
import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EventLogger implements ApplicationListener<ApplicationReadyEvent> , Ordered {
    private Locale locale;
    private final Environment environment;
    private final MonitorMessageSource messageSource;
    private boolean reportOnReadyEnabled = true;
    private boolean flameOnReadyEnabled = true;

    public EventLogger(Environment environment, MonitorMessageSource messageSource) {
        this.environment = environment;
        this.messageSource = messageSource;
    }

    @PostConstruct
    public void init(){
        String localeCode = environment.getProperty("launch.monitor.locale", "auto");
        if (localeCode.isBlank() || "auto".equalsIgnoreCase(localeCode)) {
            this.locale = Locale.getDefault();
        } else {
            this.locale = Locale.forLanguageTag(localeCode.replace('_', '-'));
        }

        this.reportOnReadyEnabled = getBooleanProperty(true,
                "launch.monitor.report",
                "launch.monitor.report.enabled");
        this.flameOnReadyEnabled = getBooleanProperty(true,
                "launch.monitor.flame",
                "launch.monitor.flame.enabled");
    }



    private String getMessage(String code, String defaultMessage, Object... args) {
        return messageSource.getMessage(code, defaultMessage, locale, args);
    }

    private boolean getBooleanProperty(boolean defaultValue, String... keys) {
        for (String key : keys) {
            Boolean value = environment.getProperty(key, Boolean.class);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

    public String generateFlameGraphData() {
        LaunchRecordsCtx records = EventRecorder.getCtx();
        StringBuilder sb = new StringBuilder();

        records.getEvents().forEach(e -> sb.append(String.format("springboot;%s %d%n",
                e.getPhase().replace(";", "_"), e.getDurationMs())));

        Map<String, Long> packageTime = records.getBeanRecords().stream()
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
        LaunchRecordsCtx records = EventRecorder.getCtx();
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage("report.title", "\uD83D\uDE80 Spring Boot Application Startup Report"));
        sb.append(getMessage("report.divider", "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"));

        long totalTime = records.getEvents().isEmpty() ? 0
                : records.getEvents().get(records.getEvents().size() - 1).getDurationMs();
        sb.append(getMessage("report.total.time", "Total startup time: {0} ms", totalTime)).append("\n\n");

        sb.append(getMessage("report.section.phase.details", "📊 Startup phase details:")).append("\n");
         records.getEvents().forEach(e -> {
            String bar = "█".repeat(Math.min(50, (int) e.getDurationMs() / 100));
            String phaseLabel = getMessage(e.getDesckey(), e.getPhase(), e.getArgs());
            sb.append(String.format("  %-25s %5dms %s%n",
                    phaseLabel, e.getDurationMs(), bar));
        });

        sb.append("\n").append(getMessage("report.section.slowest.beans", "🔥 Slowest Top 10 Beans:")).append("\n");
        List<BeanInitRecord> topBeans = records.getBeanRecords().stream()
                .sorted(Comparator.comparingLong(BeanInitRecord::getDurationMs).reversed())
                .limit(10)
                .toList();

        IntStream.range(0, topBeans.size())
                .forEach(i -> {
                    BeanInitRecord r = topBeans.get(i);
                    sb.append(String.format("  %d. %-40s %4dms%n",
                            i + 1, r.getBeanName(), r.getDurationMs()));
                });

        sb.append("\n").append(getMessage("report.section.optimization", "💡 Optimization suggestions:")).append("\n");
        analyzeBottlenecks(records.getBeanRecords(),
                records.getConfigRecords()
        ).forEach(s -> sb.append("  • ").append(s).append("\n"));

        return sb.toString();
    }

    public List<String> analyzeBottlenecks(List<BeanInitRecord> beanRecords,
                                           List<ConfigSourceRecord> configRecords) {
        List<String> suggestions = new ArrayList<>();

        long dbTime = beanRecords.stream()
                .filter(r -> r.getBeanName().toLowerCase().contains("datasource")
                        || r.getBeanName().toLowerCase().contains("database"))
                .mapToLong(BeanInitRecord::getDurationMs)
                .sum();
        if (dbTime > 1000) {
            suggestions.add(getMessage(
                    "report.suggestion.db.slow",
                    "Database connection initialization took {0}ms; check network or enable lazy initialization",
                    dbTime
            ));
        }

        long totalBeanTime = beanRecords.stream()
                .mapToLong(BeanInitRecord::getDurationMs)
                .sum();
        if (totalBeanTime > 3000) {
            suggestions.add(getMessage(
                    "report.suggestion.bean.total.slow",
                    "Total bean initialization time is high ({0}ms); check circular dependencies",
                    totalBeanTime
            ));
        }

        long configTime = configRecords.stream()
                .mapToLong(ConfigSourceRecord::getDurationMs)
                .sum();
        if (configTime > 500) {
            suggestions.add(getMessage(
                    "report.suggestion.config.slow",
                    "Configuration loading took {0}ms; consider reducing config file size",
                    configTime
            ));
        }

        return suggestions.isEmpty()
                ? Collections.singletonList(getMessage("report.suggestion.none", "No obvious performance bottlenecks detected"))
                : suggestions;
    }

    public String generateReport(List<TimelineEvent> events,  List<BeanInitRecord> beanRecords, List<ConfigSourceRecord> configRecords) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(getMessage("report.title", "\uD83D\uDE80 Spring Boot Application Startup Report")).append("\n");
        sb.append(getMessage("report.divider", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"));

        // total time
        long totalTime = events.isEmpty() ? 0
                : events.get(events.size() - 1).getDurationMs();
        sb.append(getMessage("report.total.time", "Total startup time: {0} ms", totalTime)).append("\n\n");

        // phase detail
        sb.append(getMessage("report.section.phase.details", "📊 Startup phase details:")).append("\n");
        for (TimelineEvent e : events) {
            int barLength = Math.min(50, (int) e.getDurationMs() / 100);
            StringBuilder bar = new StringBuilder();
            bar.append("█".repeat(Math.max(0, barLength)));
            String phaseLabel = getMessage(e.getDesckey(), e.getPhase(), e.getArgs());
            sb.append(String.format("  %-25s %5dms %s%n",
                    phaseLabel, e.getDurationMs(), bar.toString()));
        }

        // Slowest Top 10 beans
        sb.append("\n").append(getMessage("report.section.slowest.beans", "🔥 Slowest Top 10 Beans:")).append("\n");
        List<BeanInitRecord> sortedBeans = new ArrayList<>(beanRecords);
        sortedBeans.sort((r1, r2) -> Long.compare(r2.getDurationMs(), r1.getDurationMs()));

        int count = Math.min(10, sortedBeans.size());
        for (int i = 0; i < count; i++) {
            BeanInitRecord r = sortedBeans.get(i);
            sb.append(String.format("  %d. %-40s %4dms%n",
                    i + 1, r.getBeanName(), r.getDurationMs()));
        }

        // suggestions
        sb.append("\n").append(getMessage("report.section.optimization", "💡 Optimization suggestions:")).append("\n");
        List<String> suggestions = analyzeBottlenecks(beanRecords, configRecords);
        for (String s : suggestions) {
            sb.append("  • ").append(s).append("\n");
        }

        return sb.toString();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!reportOnReadyEnabled) {
            return;
        }
        System.out.println(generateTextReport());
        if (flameOnReadyEnabled) {
            System.out.println(generateFlameGraphData());
        }
    }
}
