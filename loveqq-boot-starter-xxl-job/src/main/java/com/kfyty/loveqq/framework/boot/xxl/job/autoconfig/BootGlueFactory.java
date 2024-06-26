package com.kfyty.loveqq.framework.boot.xxl.job.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.xxl.job.core.glue.GlueFactory;

import java.lang.reflect.Field;

/**
 * 描述: BootGlueFactory
 *
 * @author kfyty725
 * @date 2022/9/3 21:42
 * @email kfyty725@hotmail.com
 */
public class BootGlueFactory extends GlueFactory {
    private final AutowiredCapableSupport autowiredCapableSupport;

    public BootGlueFactory(ApplicationContext applicationContext) {
        this.autowiredCapableSupport = applicationContext.getBean(AutowiredCapableSupport.BEAN_NAME);
    }

    @Override
    public void injectService(Object instance) {
        if (instance != null) {
            this.autowiredCapableSupport.autowiredBean(null, instance);
        }
    }

    public static void refreshInstance(ApplicationContext applicationContext) {
        Field field = ReflectUtil.getField(GlueFactory.class, "glueFactory");
        ReflectUtil.setFieldValue(null, field, new BootGlueFactory(applicationContext));
    }
}
