package io.github.danburen.springlaunchmonitor.config;

import io.github.danburen.springlaunchmonitor.listener.BeanInitMonitor;
import io.github.danburen.springlaunchmonitor.listener.DataSourceMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.launch.monitor.enabled", havingValue = "true", matchIfMissing = true)
public class MonitorAutoConfiguration {

    @Bean
    public BeanInitMonitor beanInitMonitor() {
        return new BeanInitMonitor();
    }

    @Bean
    public DataSourceMonitor dataSourceMonitor() {
        return new DataSourceMonitor();
    }
}

