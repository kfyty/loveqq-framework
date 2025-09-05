package com.kfyty.loveqq.framework.core.autoconfig.beans;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

/**
 * 描述: bean 定义
 *
 * @author kfyty725
 * @date 2021/5/22 11:13
 * @email kfyty725@hotmail.com
 */
public interface BeanDefinition {
    /**
     * BeanDefinition 排序比较器
     */
    Comparator<BeanDefinition> BEAN_DEFINITION_COMPARATOR = Comparator
            .comparing((BeanDefinition e) -> InternalPriority.class.isAssignableFrom(e.getBeanType()) ? Order.HIGHEST_PRECEDENCE : Order.LOWEST_PRECEDENCE)
            .thenComparing(e -> BeanUtil.getBeanOrder((BeanDefinition) e))
            .thenComparing(BeanDefinition::getBeanName);

    /**
     * 单例作用域
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 原型作用域
     */
    String SCOPE_PROTOTYPE = "prototype";

    /**
     * 刷新作用域
     */
    String SCOPE_REFRESH = "refresh";

    /**
     * 线程作用域
     */
    String SCOPE_THREAD = "thread";

    /**
     * bean name，唯一
     */
    String getBeanName();

    /**
     * 设置 bean name
     */
    void setBeanName(String beanName);

    /**
     * bean 的类型
     */
    Class<?> getBeanType();

    /**
     * 设置 bean 类型
     */
    void setBeanType(Class<?> beanType);

    /**
     * bean 的作用域
     */
    String getScope();

    /**
     * 是否启用作用域代理
     *
     * @return true if scope proxy
     */
    boolean isScopeProxy();

    /**
     * 设置 bean 作用域
     */
    void setScope(String scope);

    /**
     * 设置是否启用作用域代理
     *
     * @param isScopeProxy 是否启用作用域代理
     */
    void setScopeProxy(boolean isScopeProxy);

    /**
     * 是否延迟初始化
     *
     * @return true if lazy init
     */
    boolean isLazyInit();

    /**
     * 是否代理懒加载的 bean
     *
     * @return true if lazy proxy
     */
    boolean isLazyProxy();

    /**
     * 设置是否延迟初始化
     *
     * @param isLazyInit 是否延迟初始化
     */
    void setLazyInit(boolean isLazyInit);

    /**
     * 设置是否代理懒加载的 bean
     *
     * @param isLazyProxy 是否代理懒加载
     */
    void setLazyProxy(boolean isLazyProxy);

    /**
     * 是否单例
     */
    boolean isSingleton();

    /**
     * 是否主实例
     *
     * @return true if primary bean
     */
    boolean isPrimary();

    /**
     * 是否是 {@link FactoryBean}
     *
     * @return true if FactoryBean
     */
    boolean isFactoryBean();

    /**
     * 是否是自动装配的候选者
     */
    boolean isAutowireCandidate();

    /**
     * 设置是否是自动装配的候选者，只对针对类型装配有效
     */
    void setAutowireCandidate(boolean autowireCandidate);

    /**
     * 是否是 {@link com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean} 定义的 bean
     *
     * @return true if method bean
     */
    boolean isMethodBean();

    /**
     * 获取 bean 方法
     *
     * @return bean method
     */
    Method getBeanMethod();

    /**
     * 获取初始化方法
     *
     * @return init method
     */
    Method getInitMethod(Object bean);

    /**
     * 获取销毁方法
     *
     * @return destroy method
     */
    Method getDestroyMethod(Object bean);

    /**
     * 设置初始化方法
     *
     * @param initMethod 初始化方法
     */
    void setInitMethod(String initMethod);

    /**
     * 设置销毁方法
     *
     * @param destroyMethod 销毁方法
     */
    void setDestroyMethod(String destroyMethod);

    /**
     * 获取初始化方法
     *
     * @return init method
     */
    String getInitMethod();

    /**
     * 获取销毁方法
     *
     * @return destroy method
     */
    String getDestroyMethod();

    /**
     * 添加默认的构造器参数，参数索引从 0 开始
     * 其他的参数将从 bean 工厂获取
     */
    BeanDefinition addConstructorArgs(Class<?> argType, Object arg);

    /**
     * 添加属性值
     *
     * @param propertyValue 属性值配置
     */
    BeanDefinition addPropertyValue(PropertyValue propertyValue);

    /**
     * 获取构造器参数
     * 实际用于创建 bean 的构造器参数
     */
    List<Pair<Class<?>, Object>> getConstructArgs();

    /**
     * 获取默认的构造器参数
     * 定义 bean 定义时的构造器参数
     */
    List<Pair<Class<?>, Object>> getDefaultConstructArgs();

    /**
     * 获取构造器参数类型
     */
    Class<?>[] getConstructArgTypes();

    /**
     * 获取构造器参数值
     */
    Object[] getConstructArgValues();

    /**
     * 获取属性配置
     *
     * @return 属性配置
     */
    List<PropertyValue> getPropertyValues();

    /**
     * 创建 bean 实例
     */
    Object createInstance(ApplicationContext context);
}
