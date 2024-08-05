package com.kfyty.loveqq.framework.javafx.core;

import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import javafx.application.Application;

/**
 * 描述: 抽象应用
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractApplication extends Application implements LifeCycleBinder {
    /**
     * 由于 {@link Application} 无法被 ioc 管理，这里初始化时，获取 ioc 内的属性赋值
     */
    @Override
    public void init() throws Exception {
        super.init();
        AbstractApplication iocManaged = IOC.getBean(this.getClass());
        BeanUtil.copyProperties(iocManaged, this);
    }
}
