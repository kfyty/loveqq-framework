package com.kfyty.boot.beans;

import com.kfyty.util.CommonUtil;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/21 15:30
 * @email kfyty725@hotmail.com
 */
public class BeanResources {
    /**
     * bean 类型
     */
    @Getter
    private final Class<?> beanType;

    /**
     * bean 实例，key -> bean name
     */
    private final Map<String, Object> beans;

    public BeanResources(Class<?> clazz, Object bean) {
        this(CommonUtil.convert2BeanName(clazz.getSimpleName()), clazz, bean);
    }

    public BeanResources(String name, Class<?> clazz, Object bean) {
        this.beanType = clazz;
        this.beans = new ConcurrentHashMap<>(2);
        this.beans.put(name, bean);
    }

    public boolean isSingleton() {
        return beans.size() == 1;
    }

    public void addBean(String name, Object bean) {
        if(this.beans.containsKey(name)) {
            throw new IllegalArgumentException("bean existed of name: " + name);
        }
        this.beans.put(name, bean);
    }

    public Map<String, Object> getBeans() {
        return this.beans;
    }

    public Object getBean(Class<?> clazz) {
        if(isSingleton()) {
            return this.beans.values().iterator().next();
        }
        throw new IllegalArgumentException("more than one bean found of type: " + clazz.getName());
    }

    public Object getBean(String name) {
        return this.beans.get(name);
    }
}
