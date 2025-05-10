package com.kfyty.loveqq.framework.boot.i18n;

import com.kfyty.loveqq.framework.core.i18n.LocaleResolver;

import java.util.Locale;

/**
 * 描述: 默认实现
 *
 * @author kfyty725
 * @date 2024/11/17 20:49
 * @email kfyty725@hotmail.com
 */
public class DefaultLocaleResolver implements LocaleResolver {

    @Override
    public Locale resolve() {
        return Locale.getDefault();
    }
}
