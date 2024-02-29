package com.kfyty.javafx.core.factory;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import javafx.util.Callback;

/**
 * 描述: javafx 控制器工厂，桥接到 ioc
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class ControllerFactory implements Callback<Class<?>, Object> {
    @Autowired
    private BeanFactory beanFactory;

    @Override
    public Object call(Class<?> param) {
        return this.beanFactory.getBean(param);
    }
}
