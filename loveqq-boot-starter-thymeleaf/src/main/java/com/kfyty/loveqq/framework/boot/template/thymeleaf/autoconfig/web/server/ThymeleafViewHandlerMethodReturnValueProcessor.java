package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web.server;

import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodReturnValueProcessor;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebApplication;

import java.io.StringWriter;
import java.util.Locale;

/**
 * 描述: thymeleaf 视图解析器
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ThymeleafViewHandlerMethodReturnValueProcessor implements BeanFactoryAware, ServerHandlerMethodReturnValueProcessor {
    private BeanFactory beanFactory;

    private TemplateEngine templateEngine;

    private IWebApplication webApplication;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnValue != null && CharSequence.class.isAssignableFrom(returnValue.getClass());
    }

    @Override
    public Object processReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        this.ensureEngine();
        String template = returnValue.toString();
        AbstractContext context = this.buildWebContext(container);
        context.setVariable("request", container.getRequest());
        context.setVariable("response", container.getResponse());
        try (StringWriter out = new StringWriter()) {
            container.getResponse().setContentType("text/html;charset=utf-8");
            this.templateEngine.process(template, context, out);
            return out.toString();
        }
    }

    protected void ensureEngine() {
        if (this.templateEngine == null) {
            this.templateEngine = this.beanFactory.getBean(TemplateEngine.class);
            this.webApplication = this.beanFactory.getBean(IWebApplication.class);
        }
    }

    protected AbstractContext buildWebContext(ModelViewContainer container) {
        if (this.webApplication instanceof NettyServerWebApplication) {
            NettyServerWebExchange webExchange = ((NettyServerWebApplication) this.webApplication).buildExchange(container.getRequest(), container.getResponse());
            return new WebContext(webExchange, Locale.getDefault(), container.getModel());
        }
        return new Context(Locale.getDefault(), container.getModel());
    }
}
