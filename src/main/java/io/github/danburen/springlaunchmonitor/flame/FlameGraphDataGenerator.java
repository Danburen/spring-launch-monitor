package io.github.danburen.springlaunchmonitor.spring.flame;

import io.github.danburen.springlaunchmonitor.data.LaunchRecordsCtx;

public interface FlameGraphDataGenerator {
    String generate(LaunchRecordsCtx records);

    String generateJsonTree(LaunchRecordsCtx records);

    String generateHtml(LaunchRecordsCtx records, String reportText, HtmlI18nLabels labels);
}

