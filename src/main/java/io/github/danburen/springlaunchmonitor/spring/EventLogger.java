package io.github.danburen.springlaunchmonitor.spring;

import io.github.danburen.springlaunchmonitor.config.LaunchMonitorProperties;
import io.github.danburen.springlaunchmonitor.data.BeanInitRecord;
import io.github.danburen.springlaunchmonitor.data.ConfigSourceRecord;
import io.github.danburen.springlaunchmonitor.data.LaunchRecordsCtx;
import io.github.danburen.springlaunchmonitor.flame.FlameGraphDataGenerator;
import io.github.danburen.springlaunchmonitor.flame.HtmlI18nLabels;
import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
public class EventLogger implements ApplicationListener<ApplicationReadyEvent> , Ordered {
    private Locale locale;
    private final LaunchMonitorProperties properties;
    private final MonitorMessageSource messageSource;
    private final FlameGraphDataGenerator flameGraphDataGenerator;
    private boolean reportOnReadyEnabled = true;
    private boolean flameConsoleEnabled = false;
    private boolean flameJsonEnabled = true;
    private boolean flameHtmlEnabled = true;
    private String outputDir = "build/reports/spring-launch-monitor";

    public EventLogger(LaunchMonitorProperties properties,
                       MonitorMessageSource messageSource,
                       FlameGraphDataGenerator flameGraphDataGenerator) {
        this.properties = properties;
        this.messageSource = messageSource;
        this.flameGraphDataGenerator = flameGraphDataGenerator;
        init();
    }

    public void init(){
        String localeCode = properties.getLocale();
        if (localeCode == null || localeCode.isBlank() || "auto".equalsIgnoreCase(localeCode)) {
            this.locale = Locale.getDefault();
        } else {
            this.locale = Locale.forLanguageTag(localeCode.replace('_', '-'));
        }

        this.reportOnReadyEnabled = properties.isReport();
        LaunchMonitorProperties.FlameProperties flame = properties.getFlame();
        this.flameConsoleEnabled = flame != null && flame.isConsole();
        this.flameJsonEnabled = flame == null || flame.isJson();
        this.flameHtmlEnabled = flame == null || flame.isHtml();
        this.outputDir = properties.getOutputDir();
    }



    private String getMessage(String code, String defaultMessage, Object... args) {
        return messageSource.getMessage(code, defaultMessage, locale, args);
    }


    public String generateFlameGraphData() {
        return flameGraphDataGenerator.generate(EventRecorder.getCtx());
    }

    public String generateFlameJsonTree() {
        return flameGraphDataGenerator.generateJsonTree(EventRecorder.getCtx());
    }

    public String generateFlameHtml(String reportText) {
        return flameGraphDataGenerator.generateHtml(EventRecorder.getCtx(), reportText, buildHtmlLabels());
    }

    private HtmlI18nLabels buildHtmlLabels() {
        return new HtmlI18nLabels(
                locale == null ? "en" : locale.toLanguageTag(),
                getMessage("html.page.title", "Spring Launch Monitor Startup Report"),
                getMessage("html.section.startup.report", "Startup Report"),
                getMessage("html.section.package.tree", "Package Flame Tree"),
                getMessage("html.button.expand.all", "Expand All"),
                getMessage("html.button.collapse.all", "Collapse All"),
                getMessage("html.tree.hint", "Package delay tree is expanded by default. Each bar width is proportional to root total latency."),
                getMessage("html.metrics.template", "total={0}ms, self={1}ms, beans={2}")
        );
    }

    public String generateTextReport() {
        LaunchRecordsCtx records = EventRecorder.getCtx();
        StringBuilder sb = new StringBuilder();
        sb.append("Spring Boot Application Startup Report");
        sb.append("\n===========================================\n");

        long totalTime = records.getEvents().isEmpty() ? 0
                : records.getEvents().get(records.getEvents().size() - 1).getDurationMs();
        sb.append(MessageFormat.format("Total startup time: {0} ms", totalTime)).append("\n\n");

        sb.append("Startup phase details:").append("\n");
        records.getEvents().forEach(e -> {
            String bar = "#".repeat(Math.min(50, (int) e.getDurationMs() / 100));
            String phaseLabel = e.getPhase();
            sb.append(String.format("  %-25s %5dms %s%n",
                    phaseLabel, e.getDurationMs(), bar));
        });
        sb.append("Total beans: ").append(records.getBeanRecords().size()).append("\n");

        sb.append("\n").append("Slowest Top 10 Beans:").append("\n");
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

        sb.append("\n").append("Optimization suggestions:").append("\n");
        analyzeBottlenecksEnglish(records.getBeanRecords(),
                records.getConfigRecords()
        ).forEach(s -> sb.append("  - ").append(s).append("\n"));
        return sb.toString();
    }

    public String generateLocalizedHtmlReport() {
        LaunchRecordsCtx records = EventRecorder.getCtx();
        StringBuilder sb = new StringBuilder();

        long totalTime = records.getEvents().isEmpty() ? 0
                : records.getEvents().get(records.getEvents().size() - 1).getDurationMs();
        sb.append("<p class=\"report-total\">")
                .append(escapeHtml(getMessage("report.total.time", "Total startup time: {0} ms", totalTime)))
                .append("</p>");

        sb.append("<h3>").append(escapeHtml(getMessage("report.section.phase.details", "Startup phase details:"))).append("</h3>");
        sb.append("<ul>");
        records.getEvents().forEach(e -> {
            String bar = "#".repeat(Math.min(50, (int) e.getDurationMs() / 100));
            String phaseLabel = getMessage(e.getDesckey(), e.getPhase(), e.getArgs());
            sb.append("<li><span class=\"phase-label\">")
                    .append(escapeHtml(phaseLabel))
                    .append("</span> <span class=\"phase-metric\">")
                    .append(e.getDurationMs())
                    .append("ms</span> <span class=\"phase-bar\">")
                    .append(escapeHtml(bar))
                    .append("</span></li>");
        });
        sb.append("</ul>");

        sb.append("<h3>").append(escapeHtml(getMessage("report.section.slowest.beans", "Slowest Top 10 Beans:"))).append("</h3>");
        sb.append("<ol>");
        List<BeanInitRecord> topBeans = records.getBeanRecords().stream()
                .sorted(Comparator.comparingLong(BeanInitRecord::getDurationMs).reversed())
                .limit(10)
                .toList();
        IntStream.range(0, topBeans.size())
                .forEach(i -> {
                    BeanInitRecord r = topBeans.get(i);
                    sb.append("<li>")
                            .append(escapeHtml(r.getBeanName()))
                            .append(" <span class=\"bean-metric\">")
                            .append(r.getDurationMs())
                            .append("ms</span></li>");
                });
        sb.append("</ol>");

        sb.append("<h3>").append(escapeHtml(getMessage("report.section.optimization", "Optimization suggestions:"))).append("</h3>");
        sb.append("<ul>");
        analyzeBottlenecks(records.getBeanRecords(), records.getConfigRecords())
                .forEach(s -> sb.append("<li>").append(escapeHtml(s)).append("</li>"));
        sb.append("</ul>");
        sb.append("</div>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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

    public List<String> analyzeBottlenecksEnglish(List<BeanInitRecord> beanRecords,
                                                  List<ConfigSourceRecord> configRecords) {
        List<String> suggestions = new ArrayList<>();

        long dbTime = beanRecords.stream()
                .filter(r -> r.getBeanName().toLowerCase().contains("datasource")
                        || r.getBeanName().toLowerCase().contains("database"))
                .mapToLong(BeanInitRecord::getDurationMs)
                .sum();
        if (dbTime > 1000) {
            suggestions.add(MessageFormat.format(
                    "Database connection initialization took {0}ms; check network or enable lazy initialization",
                    dbTime
            ));
        }

        long totalBeanTime = beanRecords.stream().mapToLong(BeanInitRecord::getDurationMs).sum();
        if (totalBeanTime > 3000) {
            suggestions.add(MessageFormat.format(
                    "Total bean initialization time is high ({0}ms); check circular dependencies",
                    totalBeanTime
            ));
        }

        long configTime = configRecords.stream().mapToLong(ConfigSourceRecord::getDurationMs).sum();
        if (configTime > 500) {
            suggestions.add(MessageFormat.format(
                    "Configuration loading took {0}ms; consider reducing config file size",
                    configTime
            ));
        }

        return suggestions.isEmpty()
                ? Collections.singletonList("No obvious performance bottlenecks detected")
                : suggestions;
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
        String reportText = generateTextReport();
        log.info("\n{}", reportText);
        if (flameConsoleEnabled) {
            log.info("\n{}", generateFlameGraphData());
        }
        if (flameJsonEnabled || flameHtmlEnabled) {
            writeFlameArtifacts(generateLocalizedHtmlReport());
        }
    }

    private void writeFlameArtifacts(String reportText) {
        Path baseDir = Path.of(outputDir == null || outputDir.isBlank()
                ? "build/reports/spring-launch-monitor"
                : outputDir);

        try {
            Files.createDirectories(baseDir);
            if (flameJsonEnabled) {
                Path jsonPath = baseDir.resolve("flame-tree.json");
                Files.writeString(jsonPath, generateFlameJsonTree(), StandardCharsets.UTF_8);
                EventLogger.log.info("JSON tree written: {}", jsonPath.toUri());
            }
            if (flameHtmlEnabled) {
                Path htmlPath = baseDir.resolve("flame-tree.html");
                Files.writeString(htmlPath, generateFlameHtml(reportText), StandardCharsets.UTF_8);
                EventLogger.log.info("HTML flame tree written: {}", htmlPath.toUri());
            }
        } catch (IOException e) {
            EventLogger.log.info("Failed to write flame artifacts: " + e.getMessage());
        }
    }
}
