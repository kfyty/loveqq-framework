package com.kfyty.loveqq.framework.core.autoconfig.beans;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.property.PropertyValue;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;

import java.util.Comparator;
import java.util.List;

import static com.kfyty.loveqq.framework.core.utils.BeanUtil.getBeanOrder;

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
     */
    List<Pair<Class<?>, Object>> getConstructArgs();

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
