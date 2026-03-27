package io.github.danburen.springlaunchmonitor.spring.listener;

import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

public class DataSourceMonitor implements BeanPostProcessor {
    private static final boolean JPA_PRESENT = isJpaPresent();

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
        if (bean instanceof DataSource && !(bean instanceof Proxy)) {
            return createDataSourceProxy((DataSource) bean, beanName);
        }

        if (JPA_PRESENT && isEntityManagerFactoryBean(bean)) {
            return createJpaProxy(bean, beanName);
        }

        return bean;
    }

    private boolean isEntityManagerFactoryBean(Object bean) {
        return bean.getClass().getName()
                .contains("LocalContainerEntityManagerFactoryBean");
    }

    private Object createJpaProxy(Object target, String beanName) {
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
                            EventRecorder.recordEvent("JpaInitialization",
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
                            EventRecorder.recordEvent("FirstDBConnection",
                                    beanName + "First connection", duration);
                        }
                    }
                    return method.invoke(target, args);
                }
        );
    }
}
