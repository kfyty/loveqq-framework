package com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;

import java.util.Map;

/**
 * 描述: 处理器方法参数解析器
 *
 * @author kfyty725
 * @date 2021/6/4 9:45
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractHandlerMethodArgumentResolver implements ServletHandlerMethodArgumentResolver, BeanFactoryAware {
    /**
     * 默认的数据绑定器 class
     */
    protected static final Class<?> DEFAULT_DATA_BINDER_CLASS = ReflectUtil.load("com.kfyty.loveqq.framework.boot.context.env.DefaultDataBinder");

    /**
     * 默认的泛型配置属性解析器
     */
    protected static final Class<?> DEFAULT_GENERIC_PROPERTIES_CONTEXT_CLASS = ReflectUtil.load("com.kfyty.loveqq.framework.boot.context.env.DefaultGenericPropertiesContext");

    /**
     * {@link BeanFactory}
     */
    protected BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public DataBinder createDataBinder(String name, String value) {
        DataBinder dataBinder = this.createDataBinder();
        dataBinder.setProperty(name, value);
        return dataBinder;
    }

    public DataBinder createDataBinder(Map<String, String> params) {
        DataBinder dataBinder = this.createDataBinder();
        params.forEach(dataBinder::setProperty);
        return dataBinder;
    }

    protected DataBinder createDataBinder() {
        if (this.beanFactory != null) {
            return this.beanFactory.getBean(DataBinder.class).clone();
        }
        DataBinder dataBinder = (DataBinder) ReflectUtil.newInstance(DEFAULT_DATA_BINDER_CLASS);
        dataBinder.setPropertyContext(this.createPropertiesContext());
        dataBinder.getPropertyContext().setDataBinder(dataBinder);
        return dataBinder;
    }

    protected GenericPropertiesContext createPropertiesContext() {
        return (GenericPropertiesContext) ReflectUtil.newInstance(DEFAULT_GENERIC_PROPERTIES_CONTEXT_CLASS);
    }
}
