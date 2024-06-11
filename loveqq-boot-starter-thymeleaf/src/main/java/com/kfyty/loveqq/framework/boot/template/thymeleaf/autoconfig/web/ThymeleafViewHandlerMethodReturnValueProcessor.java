package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web;

import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver.ServletHandlerMethodReturnValueProcessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.IServletWebApplication;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.Writer;
import java.util.Locale;

/**
 * 描述: thymeleaf 视图解析器
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ThymeleafViewHandlerMethodReturnValueProcessor implements ServletHandlerMethodReturnValueProcessor, BeanFactoryAware {
    private BeanFactory beanFactory;

    private TemplateEngine templateEngine;

    private IServletWebApplication webApplication;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnType != null && returnType.getReturnType() == String.class;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer<HttpServletRequest, HttpServletResponse> container) throws Exception {
        this.ensureEngine();
        String template = returnValue.toString();
        AbstractContext context = this.buildWebContext(container);
        context.setVariable("request", container.getRequest());
        context.setVariable("response", container.getResponse());
        try (Writer out = container.getResponse().getWriter()) {
            container.getResponse().setContentType("text/html;charset=utf-8");
            this.templateEngine.process(template, context, out);
            out.flush();
        }
    }

    protected void ensureEngine() {
        if (this.templateEngine == null) {
            this.templateEngine = this.beanFactory.getBean(TemplateEngine.class);
            this.webApplication = this.beanFactory.getBean(IServletWebApplication.class);
        }
    }

    protected AbstractContext buildWebContext(ModelViewContainer<HttpServletRequest, HttpServletResponse> container) {
        if (this.webApplication instanceof JakartaServletWebApplication) {
            IServletWebExchange webExchange = ((JakartaServletWebApplication) this.webApplication).buildExchange(container.getRequest(), container.getResponse());
            return new WebContext(webExchange, Locale.getDefault(), container.getModel());
        }
        return new Context(Locale.getDefault(), container.getModel());
    }
}
