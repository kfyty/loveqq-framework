package com.kfyty.loveqq.framework.boot.i18n;

import com.kfyty.loveqq.framework.core.i18n.LocaleResolver;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * 描述: 默认实现
 *
 * @author kfyty725
 * @date 2024/11/17 20:49
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class DefaultLocaleResolver implements LocaleResolver {
    /**
     * 默认的地区
     */
    private final Locale defaultLocale;

    @Override
    public Locale resolve() {
        if (this.defaultLocale != null) {
            return this.defaultLocale;
        }
        return Locale.getDefault();
    }
}
