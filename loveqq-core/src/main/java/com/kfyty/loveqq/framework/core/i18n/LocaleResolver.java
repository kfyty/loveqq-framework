package com.kfyty.loveqq.framework.core.i18n;

import java.util.Locale;

/**
 * 描述: {@link java.util.Locale} 解析器
 *
 * @author kfyty725
 * @date 2024/11/17 20:49
 * @email kfyty725@hotmail.com
 */
public interface LocaleResolver {
    /**
     * 解析返回 {@link Locale}
     *
     * @return {@link Locale}
     */
    Locale resolve();
}
