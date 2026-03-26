package io.github.danburen.springlaunchmonitor.listener;

import io.github.danburen.springlaunchmonitor.util.StartupTimeline;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

public class DataSourceMonitor implements BeanPostProcessor {
    private static final boolean JPA_PRESENT = isJpaPresent();

    // 检测JPA是否存在
    private static boolean isJpaPresent() {
        try {
            Class.forName("javax.persistence.EntityManagerFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {

        // 1. 监控DataSource（始终启用）
        if (bean instanceof DataSource && !(bean instanceof Proxy)) {
            return createDataSourceProxy((DataSource) bean, beanName);
        }

        // 2. 监控JPA（条件化）
        if (JPA_PRESENT && isEntityManagerFactoryBean(bean)) {
            return createJpaProxy(bean, beanName);
        }

        return bean;
    }

    private boolean isEntityManagerFactoryBean(Object bean) {
        // 通过反射检测，避免直接依赖类
        return bean.getClass().getName()
                .contains("LocalContainerEntityManagerFactoryBean");
    }

    private Object createJpaProxy(Object target, String beanName) {
        // 反射获取FactoryBean的createNativeEntityManagerFactory方法
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    if ("createNativeEntityManagerFactory".equals(method.getName()) ||
                            "afterPropertiesSet".equals(method.getName())) {
                        long start = System.nanoTime();
                        try {
                            return method.invoke(target, args);
                        } finally {
                            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                            StartupTimeline.recordEvent("JpaInitialization",
                                    beanName + "." + method.getName(), duration);
                        }
                    }
                    return method.invoke(target, args);
                }
        );
    }

    private DataSource createDataSourceProxy(DataSource target, String beanName) {
        return (DataSource) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) {
                        long start = System.nanoTime();
                        try {
                            return method.invoke(target, args);
                        } finally {
                            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                            // 记录首次连接耗时（通常是最慢的）
                            StartupTimeline.recordEvent("FirstDBConnection",
                                    beanName + "首次连接", duration);
                        }
                    }
                    return method.invoke(target, args);
                }
        );
    }
}
