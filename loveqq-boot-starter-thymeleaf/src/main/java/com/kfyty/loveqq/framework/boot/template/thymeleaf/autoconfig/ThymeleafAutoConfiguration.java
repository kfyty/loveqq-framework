package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig;

import com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.dialect.LoveqqStandardDialect;
import com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.expression.DefaultVariableExpressionEvaluator;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnWebApplication;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;

import java.util.Set;

/**
 * 描述: thymeleaf 自动配置
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ThymeleafAutoConfiguration {
    @Autowired(required = false)
    private Set<IDialect> dialects;

    @Autowired(required = false)
    private Set<IProcessor> processors;

    @Autowired
    private ThymeleafProperties thymeleafProperties;

    @Autowired(required = false)
    private IStandardVariableExpressionEvaluator standardVariableExpressionEvaluator;

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public ITemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setCacheable(this.thymeleafProperties.isCacheable());
        templateResolver.setTemplateMode(this.thymeleafProperties.getTemplateMode());
        templateResolver.setCharacterEncoding(this.thymeleafProperties.getCharacterEncoding());
        templateResolver.setPrefix(this.thymeleafProperties.getPrefix());
        templateResolver.setSuffix(this.thymeleafProperties.getSuffix());
        return templateResolver;
    }

    @ConditionalOnWebApplication
    @Bean(resolveNested = false, independent = true)
    public ITemplateResolver webApplicationTemplateResolver(ThymeleafProperties thymeleafProperties, IWebApplication webApplication) {
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setCacheable(thymeleafProperties.isCacheable());
        templateResolver.setTemplateMode(thymeleafProperties.getTemplateMode());
        templateResolver.setCharacterEncoding(thymeleafProperties.getCharacterEncoding());
        templateResolver.setPrefix(thymeleafProperties.getPrefix());
        templateResolver.setSuffix(thymeleafProperties.getSuffix());
        return templateResolver;
    }

    @Bean(resolveNested = false, independent = true)
    public TemplateEngine templateEngine(ITemplateResolver templateResolver) {
        LoveqqStandardDialect standardDialect = new LoveqqStandardDialect(this.processors);
        standardDialect.setVariableExpressionEvaluator(new DefaultVariableExpressionEvaluator(this.standardVariableExpressionEvaluator));

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setDialect(standardDialect);
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setAdditionalDialects(this.dialects);
        return templateEngine;
    }
}
