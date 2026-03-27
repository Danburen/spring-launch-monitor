package io.github.danburen.springlaunchmonitor.spring.listener;

import io.github.danburen.springlaunchmonitor.util.EventRecorder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BeanInitMonitor implements BeanPostProcessor, PriorityOrdered {
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private final Map<String, Long> beanStartTimes = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        beanStartTimes.put(beanName, System.nanoTime());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        Long startTime = beanStartTimes.remove(beanName);
        if (startTime != null) {
            long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            EventRecorder.recordBeanInit(beanName,
                    bean.getClass().getName(),
                    duration,
                    getBeanDependencies(bean));
        }
        return bean;
    }

    private List<String> getBeanDependencies(Object bean) {
        List<String> dependencies = new ArrayList<>();
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                dependencies.add(field.getType().getSimpleName());
            }
        }
        return dependencies;
    }
}
