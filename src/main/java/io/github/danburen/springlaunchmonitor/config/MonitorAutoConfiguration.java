package io.github.danburen.springlaunchmonitor.config;

import io.github.danburen.springlaunchmonitor.spring.EventLogger;
import io.github.danburen.springlaunchmonitor.spring.MonitorMessageSource;
import io.github.danburen.springlaunchmonitor.spring.listener.BeanInitMonitor;
import io.github.danburen.springlaunchmonitor.spring.listener.DataSourceMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnProperty(name = "launch.monitor.enable", havingValue = "true", matchIfMissing = true)
public class MonitorAutoConfiguration {

    @Bean
    public BeanInitMonitor beanInitMonitor() {
        return new BeanInitMonitor();
    }

    @Bean
    public DataSourceMonitor dataSourceMonitor() {
        return new DataSourceMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    public MonitorMessageSource monitorMessageSource(ResourceLoader resourceLoader) {
        return new MonitorMessageSource(resourceLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventLogger eventLogger(Environment environment, MonitorMessageSource monitorMessageSource) {
        return new EventLogger(environment, monitorMessageSource);
    }
}

