package com.kfyty.loveqq.framework.boot.dubbo.autoconfig.override;

import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.model.ModuleModel;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/10/29 20:31
 * @email kfyty725@hotmail.com
 */
public class LoveqqReferenceConfig<T> extends ReferenceConfig<T> implements FactoryBean<T> {
    /**
     * bean type
     */
    private final Class<T> beanType;

    public LoveqqReferenceConfig(Class<T> beanType) {
        super();
        this.beanType = beanType;
    }

    public LoveqqReferenceConfig(Class<T> beanType, Reference reference) {
        super(reference);
        this.beanType = beanType;
    }

    public LoveqqReferenceConfig(Class<T> beanType, ModuleModel moduleModel, Reference reference) {
        super(moduleModel, reference);
        this.beanType = beanType;
    }

    @Override
    public Class<?> getBeanType() {
        return this.beanType;
    }

    @Override
    public T getObject() {
        return this.get();
    }
}
