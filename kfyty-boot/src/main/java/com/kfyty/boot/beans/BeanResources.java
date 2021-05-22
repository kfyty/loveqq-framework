package com.kfyty.boot.beans;

import com.kfyty.util.CommonUtil;

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
     * bean 实例，key -> bean name
     */
    private final Map<String, Object> beans;

    public BeanResources(Object bean) {
        this(CommonUtil.convert2BeanName(bean.getClass().getSimpleName()), bean);
    }

    public BeanResources(String name, Object bean) {
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
