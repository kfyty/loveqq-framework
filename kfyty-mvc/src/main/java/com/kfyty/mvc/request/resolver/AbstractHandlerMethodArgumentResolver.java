package com.kfyty.mvc.request.resolver;

import com.kfyty.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.env.DataBinder;
import com.kfyty.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.core.utils.ReflectUtil;

import java.util.Map;

/**
 * 描述: 处理器方法参数解析器
 *
 * @author kfyty725
 * @date 2021/6/4 9:45
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver, BeanFactoryAware {
    /**
     * 默认的数据绑定器 class
     */
    protected static final Class<?> DEFAULT_DATA_BINDER_CLASS = ReflectUtil.load("com.kfyty.boot.context.env.DefaultDataBinder");

    /**
     * 默认的泛型配置属性解析器
     */
    protected static final Class<?> DEFAULT_GENERIC_PROPERTIES_CONTEXT_CLASS = ReflectUtil.load("com.kfyty.boot.context.env.DefaultGenericPropertiesContext");

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
        DataBinder dataBinder = this.beanFactory != null ? this.beanFactory.getBean(DataBinder.class).clone() : (DataBinder) ReflectUtil.newInstance(DEFAULT_DATA_BINDER_CLASS);
        GenericPropertiesContext propertiesContext = this.createPropertiesContext();
        propertiesContext.setDataBinder(dataBinder);
        dataBinder.setPropertyContext(propertiesContext);
        return dataBinder;
    }

    protected GenericPropertiesContext createPropertiesContext() {
        return (GenericPropertiesContext) ReflectUtil.newInstance(DEFAULT_GENERIC_PROPERTIES_CONTEXT_CLASS);
    }
}
