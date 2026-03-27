package io.github.danburen.springlaunchmonitor.spring;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

public class MonitorMessageSource {
    private final ResourceLoader resourceLoader;
    public static final Map<String, Map<Locale, String>> MESSAGE = new HashMap<>();
    private final String BASENAME = "i18n/messages";

    private Locale locale;
    @Value("${launch.monitor.locale:}")
    private String localStr;
    public MonitorMessageSource(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    static {
        Map<Locale, String> en = new HashMap<>();
        en.put(Locale.ENGLISH, "Launch monitor started successfully");
        MESSAGE.put("launch.monitor.started", en);
    }

    @PostConstruct
    private void init(){
        if(localStr == null || localStr.isBlank() || "auto".equalsIgnoreCase(localStr)){
            locale = Locale.getDefault();
        }else{
            locale = Locale.forLanguageTag(localStr.replace('_', '-'));
        }
        loadLocal(locale);
    }

    public String getMessage(String code, String defaultMsg, Locale locale, Object... args) {
        Locale effectiveLocale = locale == null ? this.locale : locale;
        if (effectiveLocale == null) {
            effectiveLocale = Locale.getDefault();
        }

        String template = MESSAGE.getOrDefault(code, Collections.emptyMap())
                .getOrDefault(effectiveLocale, defaultMsg);
        if (template == null || template.isEmpty()) {
            template = defaultMsg;
        }

        Object[] safeArgs = args == null ? new Object[0] : args;
        return MessageFormat.format(template, safeArgs);
    }

    public void loadLocal(Locale locale) {
            String filename = resolveFilename(BASENAME, locale);
            Properties props = loadResource(filename);
            if (props != null) {
                this.locale = locale;
                for (String key : props.stringPropertyNames()) {
                    MESSAGE.computeIfAbsent(key, k -> new HashMap<>())
                            .put(locale, props.getProperty(key));
                }
            }
    }

    private String resolveFilename(String basename, Locale locale) {
        if (locale == null || locale.equals(Locale.ROOT)) {
            return basename + ".properties";
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();

        if (!country.isEmpty()) {
            return basename + "_" + lang + "_" + country + ".properties";
        }
        return basename + "_" + lang + ".properties";
    }

    private @Nullable Properties loadResource(String resPath) {
        Resource resource = resourceLoader.getResource("classpath:" + resPath);
        if (!resource.exists()) return null;
        Properties props = new Properties();
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            props.load(reader);
        }catch (IOException e) {
            return null;
        }
        return props;
    }
}
