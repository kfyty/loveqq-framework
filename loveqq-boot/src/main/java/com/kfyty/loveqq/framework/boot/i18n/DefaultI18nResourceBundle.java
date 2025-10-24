package com.kfyty.loveqq.framework.boot.i18n;

import com.kfyty.loveqq.framework.core.i18n.I18nResourceBundle;
import com.kfyty.loveqq.framework.core.i18n.LocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 国际化资源包
 *
 * @author kfyty725
 * @date 2024/11/17 20:49
 * @email kfyty725@hotmail.com
 */
public class DefaultI18nResourceBundle implements I18nResourceBundle {
    /**
     * 默认的地区
     */
    private final LocaleResolver resolver;

    /**
     * 解析后的资源包
     */
    private final List<String> resources;

    /**
     * 基于地区的资源包缓存
     */
    private final Map<Locale, ResourceBundle[]> resourceBundles;

    public DefaultI18nResourceBundle(LocaleResolver resolver, List<String> resources) {
        this.resolver = resolver;
        this.resources = resources;
        this.resourceBundles = new ConcurrentHashMap<>(4, 0.95F);
    }

    @Override
    public String getMessage(String code, Object... args) {
        return this.getMessage(code, this.resolver.resolve(), args);
    }

    @Override
    public String getMessage(String code, String defaultMessage, Object... args) {
        return this.getMessage(code, defaultMessage, this.resolver.resolve(), args);
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return this.getMessage(code, null, locale, args);
    }

    @Override
    public String getMessage(String code, String defaultMessage, Locale locale, Object... args) {
        if (this.resources.isEmpty()) {
            return this.getDefaultMessage(code, args, defaultMessage);
        }
        ResourceBundle[] resourceBundles = this.getResourceBundle(locale);
        for (ResourceBundle resourceBundle : resourceBundles) {
            if (resourceBundle.containsKey(code)) {
                String message = resourceBundle.getString(code);
                return args == null || args.length == 0 ? message : String.format(message, args);
            }
        }
        return this.getDefaultMessage(code, args, defaultMessage);
    }

    @Override
    public ResourceBundle[] getResourceBundle(Locale locale) {
        return this.resourceBundles.computeIfAbsent(locale, k -> this.resources.stream().map(e -> ResourceBundle.getBundle(e, k)).toArray(ResourceBundle[]::new));
    }

    protected String getDefaultMessage(String code, Object[] args, String message) {
        if (message != null) {
            return args == null || args.length == 0 ? message : String.format(message, args);
        }
        throw new MissingResourceException("Resource doesn't exists.", this.getClass().getName(), code);
    }
}
