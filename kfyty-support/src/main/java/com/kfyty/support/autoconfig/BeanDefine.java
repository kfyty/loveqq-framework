package com.kfyty.support.autoconfig;

import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 描述: bean 定义
 *
 * @author kfyty725
 * @date 2021/5/22 11:13
 * @email kfyty725@hotmail.com
 */
@Data
@Slf4j
public class BeanDefine {
    private final Class<?> beanType;

    private boolean isInstance;
    private Method initMethod;
    private Method destroyMethod;

    public BeanDefine(Class<?> beanType) {
        this(beanType, false);
    }

    public BeanDefine(Class<?> beanType, boolean isInstance) {
        this.beanType = beanType;
        this.isInstance = isInstance;
    }

    public Object createInstance() {
        Object instance = ReflectUtil.newInstance(this.beanType);
        this.isInstance = true;
        if(log.isDebugEnabled()) {
            log.debug(": instantiate bean: [{}] !", instance);
        }
        return instance;
    }

    public static BeanDefine from(Class<?> beanType, Object instance, Bean bean) {
        BeanDefine beanDefine = new BeanDefine(beanType, true);
        if(CommonUtil.notEmpty(bean.initMethod())) {
            beanDefine.setInitMethod(ReflectUtil.getMethod(instance.getClass(), bean.initMethod()));
        }
        if(CommonUtil.notEmpty(bean.destroyMethod())) {
            beanDefine.setDestroyMethod(ReflectUtil.getMethod(instance.getClass(), bean.destroyMethod()));
        }
        return beanDefine;
    }
}
