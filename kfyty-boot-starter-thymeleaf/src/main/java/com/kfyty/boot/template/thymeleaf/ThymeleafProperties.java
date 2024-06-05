package com.kfyty.boot.template.thymeleaf;

import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import lombok.Data;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("template.thymeleaf")
public class ThymeleafProperties {
    private boolean cacheable = true;
    private String characterEncoding = StandardCharsets.UTF_8.displayName();
    private String templateMode = TemplateMode.HTML.name();
    private String prefix;
    private String suffix;
}
