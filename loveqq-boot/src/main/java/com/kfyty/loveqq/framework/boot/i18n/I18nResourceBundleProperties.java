package com.kfyty.loveqq.framework.boot.i18n;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.i18n.I18nResourceBundle;
import com.kfyty.loveqq.framework.core.i18n.LocaleResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 描述: 国际化资源包
 *
 * @author kfyty725
 * @date 2024/11/17 20:49
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("k.i18n")
public class I18nResourceBundleProperties {
    /**
     * 默认的地区
     */
    private Locale defaultLocale;

    /**
     * 资源包文件
     */
    private List<String> resources;

    @Bean
    @ConditionalOnMissingBean
    public LocaleResolver defaultLocaleResolver() {
        return new DefaultLocaleResolver(this.defaultLocale);
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nResourceBundle defaultI18nResourceBundle(LocaleResolver localeResolver) {
        if (CommonUtil.empty(this.resources)) {
            return new DefaultI18nResourceBundle(localeResolver, Collections.emptyList());
        }
        return new DefaultI18nResourceBundle(localeResolver, this.resources);
    }
}
