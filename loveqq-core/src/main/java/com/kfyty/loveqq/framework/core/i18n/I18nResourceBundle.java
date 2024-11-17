package com.kfyty.loveqq.framework.core.i18n;

import java.util.Locale;

/**
 * 描述: 国际化资源包
 *
 * @author kfyty725
 * @date 2024/11/17 20:49
 * @email kfyty725@hotmail.com
 */
public interface I18nResourceBundle {
    /**
     * 获取国际化翻译后的信息
     *
     * @param code code
     * @param args 参数
     * @return i18n message
     */
    String getMessage(String code, Object[] args);

    /**
     * 获取国际化翻译后的信息
     *
     * @param code   code
     * @param args   参数
     * @param locale {@link Locale}
     * @return i18n message
     */
    String getMessage(String code, Object[] args, Locale locale);

    /**
     * 获取国际化翻译后的信息
     *
     * @param code           code
     * @param args           参数
     * @param defaultMessage 默认的消息
     * @param locale         {@link Locale}
     * @return i18n message
     */
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);
}
