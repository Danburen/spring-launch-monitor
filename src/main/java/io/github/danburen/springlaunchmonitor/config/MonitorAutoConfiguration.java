package io.github.danburen.springlaunchmonitor.config;

import io.github.danburen.springlaunchmonitor.spring.EventLogger;
import io.github.danburen.springlaunchmonitor.spring.MonitorMessageSource;
import io.github.danburen.springlaunchmonitor.flame.FlameGraphDataGenerator;
import io.github.danburen.springlaunchmonitor.flame.PackageTreeFlameGraphDataGenerator;
import io.github.danburen.springlaunchmonitor.spring.listener.BeanInitMonitor;
import io.github.danburen.springlaunchmonitor.spring.listener.DataSourceMonitor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@EnableConfigurationProperties(LaunchMonitorProperties.class)
@ConditionalOnProperty(name = "launch.monitor.enable", havingValue = "true", matchIfMissing = true)
public class MonitorAutoConfiguration {


    @Bean
    public BeanInitMonitor beanInitMonitor() {
        return new BeanInitMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "javax.sql.DataSource")
    @ConditionalOnBean(type = "javax.sql.DataSource")
    public DataSourceMonitor dataSourceMonitor() {
        return new DataSourceMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    public MonitorMessageSource monitorMessageSource(ResourceLoader resourceLoader,
                                                     LaunchMonitorProperties properties) {
        return new MonitorMessageSource(resourceLoader, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlameGraphDataGenerator flameGraphDataGenerator() {
        return new PackageTreeFlameGraphDataGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventLogger eventLogger(LaunchMonitorProperties properties,
                                   MonitorMessageSource monitorMessageSource,
                                   FlameGraphDataGenerator flameGraphDataGenerator) {
        return new EventLogger(properties, monitorMessageSource, flameGraphDataGenerator);
    }
}

