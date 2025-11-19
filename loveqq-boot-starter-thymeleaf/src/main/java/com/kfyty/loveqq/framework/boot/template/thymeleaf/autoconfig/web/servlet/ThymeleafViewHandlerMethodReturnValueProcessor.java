package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web.servlet;

import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 * 描述: thymeleaf 视图解析器
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ThymeleafViewHandlerMethodReturnValueProcessor implements BeanFactoryAware, HandlerMethodReturnValueProcessor {
    private BeanFactory beanFactory;

    private TemplateEngine templateEngine;

    private IWebApplication webApplication;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnValue instanceof CharSequence) {
            String view = ((CharSequence) returnValue).toString();
            return !view.startsWith(VIEW_REDIRECT) && !view.startsWith(VIEW_FORWARD);
        }
        return false;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        this.ensureEngine();
        String template = returnValue.toString();
        AbstractContext context = this.buildWebContext(container);
        context.setVariable("request", container.getRequest().getRawRequest());
        context.setVariable("response", container.getResponse().getRawResponse());
        try (OutputStream out = container.getResponse().getOutputStream()) {
            container.getResponse().setContentType("text/html;charset=utf-8");
            this.templateEngine.process(template, context, new OutputStreamWriter(out));
            out.flush();
        }
    }

    protected void ensureEngine() {
        if (this.templateEngine == null) {
            this.templateEngine = this.beanFactory.getBean(TemplateEngine.class);
            this.webApplication = this.beanFactory.getBean(IWebApplication.class);
        }
    }

    protected AbstractContext buildWebContext(ModelViewContainer container) {
        if (this.webApplication instanceof JakartaServletWebApplication) {
            IServletWebExchange webExchange = ((JakartaServletWebApplication) this.webApplication).buildExchange((HttpServletRequest) container.getRequest().getRawRequest(), (HttpServletResponse) container.getResponse().getRawResponse());
            return new WebContext(webExchange, Locale.getDefault(), container.getModel());
        }
        return new Context(Locale.getDefault(), container.getModel());
    }
}
