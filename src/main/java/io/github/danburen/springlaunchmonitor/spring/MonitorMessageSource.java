package io.github.danburen.springlaunchmonitor.spring;

import io.github.danburen.springlaunchmonitor.config.LaunchMonitorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

@Slf4j
public class MonitorMessageSource {
    private final ResourceLoader resourceLoader;
    private final LaunchMonitorProperties properties;
    public static final Map<String, Map<Locale, String>> MESSAGE = new HashMap<>();
    private final String BASENAME = "i18n/messages";

    private Locale locale;
    public MonitorMessageSource(ResourceLoader resourceLoader, LaunchMonitorProperties properties) {
        this.resourceLoader = resourceLoader;
        this.properties = properties;
        init();
    }
//    static {
//        Map<Locale, String> en = new HashMap<>();
//        en.put(Locale.ENGLISH, "Launch monitor started successfully");
//        MESSAGE.put("launch.monitor.started", en);
//    }

    private void init(){
        String localStr = properties.getLocale();
        if(localStr == null || localStr.isBlank() || "auto".equalsIgnoreCase(localStr)){
            locale = Locale.getDefault();
        }else{
            locale = Locale.forLanguageTag(localStr.replace('_', '-'));
        }
        loadLocal(locale);
    }

    public String getMessage(String code, String defaultMsg, Locale locale, Object... args) {
        Locale effectiveLocale = normalizeLocale(locale == null ? this.locale : locale);
        if (effectiveLocale == null) {
            effectiveLocale = normalizeLocale(Locale.getDefault());
        }

        Map<Locale, String> localized = MESSAGE.getOrDefault(code, Collections.emptyMap());
        String template = localized.get(effectiveLocale);
        if (template == null) {
            // Fallback to language-only locale, e.g. zh-CN -> zh
            template = localized.get(new Locale(effectiveLocale.getLanguage()));
        }
        if (template == null) {
            template = defaultMsg;
        }
        if (template == null || template.isEmpty()) {
            template = defaultMsg;
        }

        Object[] safeArgs = args == null ? new Object[0] : args;
        return MessageFormat.format(template, safeArgs);
    }

    public void loadLocal(Locale locale) {
            Locale normalized = normalizeLocale(locale);
            String filename = resolveFilename(BASENAME, normalized);
            Properties props = loadResource(filename);
            if (props != null) {
                this.locale = normalized;
                for (String key : props.stringPropertyNames()) {
                    MESSAGE.computeIfAbsent(key, k -> new HashMap<>())
                            .put(normalized, props.getProperty(key));
                }
            }
            log.info("Loaded messages for locale {} from {}", normalized, filename);
    }

    private Locale normalizeLocale(Locale locale) {
        if (locale == null) {
            return null;
        }
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        if (lang == null || lang.isBlank()) {
            return locale;
        }
        return country == null || country.isBlank()
                ? new Locale(lang)
                : new Locale(lang, country);
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

    private Properties loadResource(String resPath) {
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
