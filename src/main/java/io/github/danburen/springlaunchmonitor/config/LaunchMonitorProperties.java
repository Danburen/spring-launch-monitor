package io.github.danburen.springlaunchmonitor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "launch.monitor")
public class LaunchMonitorProperties {
    @Setter
    private boolean enable = true;
    @Setter
    private boolean report = true;
    private FlameProperties flame = new FlameProperties();
    @Setter
    private String outputDir = "build/reports/spring-launch-monitor";
    @Setter
    private String locale = "auto";


    public void setFlame(FlameProperties flame) {
        this.flame = flame == null ? new FlameProperties() : flame;
    }

    @Setter
    @Getter
    public static class FlameProperties {
        private boolean console = false;
        private boolean html = true;
        private boolean json = true;

    }
}

