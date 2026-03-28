package io.github.danburen.springlaunchmonitor.config;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

public class LaunchMonitorProperties {
    private boolean enable = true;
    private boolean report = true;
    private boolean flame = true;
    private String locale = "auto";

    public static LaunchMonitorProperties resolve(Environment environment) {
        LaunchMonitorProperties properties = Binder.get(environment)
                .bind("launch.monitor", Bindable.of(LaunchMonitorProperties.class))
                .orElseGet(LaunchMonitorProperties::new);

        // Backward-compatible switches.
        Boolean legacyReport = environment.getProperty("launch.monitor.report.enabled", Boolean.class);
        if (legacyReport != null) {
            properties.setReport(legacyReport);
        }

        Boolean legacyFlame = environment.getProperty("launch.monitor.flame.enabled", Boolean.class);
        if (legacyFlame != null) {
            properties.setFlame(legacyFlame);
        }

        if (properties.locale == null || properties.locale.isBlank()) {
            properties.locale = "auto";
        }
        return properties;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isReport() {
        return report;
    }

    public void setReport(boolean report) {
        this.report = report;
    }

    public boolean isFlame() {
        return flame;
    }

    public void setFlame(boolean flame) {
        this.flame = flame;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}

