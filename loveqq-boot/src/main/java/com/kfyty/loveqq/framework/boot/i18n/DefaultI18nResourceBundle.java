package com.kfyty.loveqq.framework.boot.i18n;

import com.kfyty.loveqq.framework.core.i18n.I18nResourceBundle;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.getBundle;

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
    private final Locale defaultLocale;

    /**
     * 解析后的资源包
     */
    private final List<String> resources;

    /**
     * 基于地区的资源包缓存
     */
    private final Map<Locale, List<ResourceBundle>> resourceBundles;

    public DefaultI18nResourceBundle(Locale defaultLocale, List<String> resources) {
        this.defaultLocale = defaultLocale;
        this.resources = resources;
        this.resourceBundles = new ConcurrentHashMap<>(4, 0.95F);
    }

    @Override
    public String getMessage(String code, Object[] args) {
        return this.getMessage(code, args, this.defaultLocale);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) {
        return this.getMessage(code, args, null, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (this.resources.isEmpty()) {
            return this.getDefaultMessage(code, args, defaultMessage);
        }
        List<ResourceBundle> resourceBundles = this.resourceBundles.computeIfAbsent(locale, k -> this.resources.stream().map(e -> getBundle(e, k)).collect(Collectors.toList()));
        for (ResourceBundle resourceBundle : resourceBundles) {
            if (resourceBundle.containsKey(code)) {
                String message = resourceBundle.getString(code);
                return args == null || args.length == 0 ? message : String.format(message, args);
            }
        }
        return this.getDefaultMessage(code, args, defaultMessage);
    }

    protected String getDefaultMessage(String code, Object[] args, String message) {
        if (message != null) {
            return args == null || args.length == 0 ? message : String.format(message, args);
        }
        throw new MissingResourceException("Resource doesn't exists.", this.getClass().getName(), code);
    }
}
