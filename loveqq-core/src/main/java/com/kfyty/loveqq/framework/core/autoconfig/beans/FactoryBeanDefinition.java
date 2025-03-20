package com.kfyty.loveqq.framework.core.autoconfig.beans;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.kfyty.loveqq.framework.core.utils.BeanUtil.removeFactoryBeanNamePrefix;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.newInstance;

/**
 * 描述: FactoryBean 类型的 bean 定义所衍生的 bean 定义
 *
 * @author kfyty725
 * @date 2021/6/12 12:06
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FactoryBeanDefinition extends GenericBeanDefinition {
    /**
     * FactoryBean<T> 本身的 bean name 前缀
     */
    public static final String FACTORY_BEAN_PREFIX = "&";

    /**
     * 临时 FactoryBean 对象缓存，用于获取目标 bean 定义信息
     * {@link ConditionalOnBean} 条件也需要该缓存
     */
    private static final Map<String, FactoryBean<?>> SNAPSHOT_FACTORY_BEAN_CACHE = new ConcurrentSkipListMap<>();

    /**
     * 该 bean 定义所在的工厂 bean 定义
     * 该工厂 bean 定义的构造方法，必须有一个符合 defaultConstructorArgs 参数的构造器，以支持构造临时对象
     * 其实际实例化时的构造器，可用 Autowired 进行标注
     */
    private final BeanDefinition factoryBeanDefinition;

    public FactoryBeanDefinition(BeanDefinition factoryBeanDefinition) {
        this(getFactoryBeanCache(factoryBeanDefinition), factoryBeanDefinition);
    }

    private FactoryBeanDefinition(FactoryBean<?> temp, BeanDefinition factoryBeanDefinition) {
        super(removeFactoryBeanNamePrefix(factoryBeanDefinition.getBeanName()), temp.getBeanType(), factoryBeanDefinition.getScope(), factoryBeanDefinition.isScopeProxy(), factoryBeanDefinition.isLazyInit(), factoryBeanDefinition.isLazyProxy());
        this.factoryBeanDefinition = factoryBeanDefinition;
        if (temp.isSingleton()) {
            this.factoryBeanDefinition.setScope(SCOPE_SINGLETON);
        }
    }

    @Override
    public boolean isPrimary() {
        return this.factoryBeanDefinition.isPrimary();
    }

    /**
     * 因为该 bean 可能被注入到自身而导致递归提前创建，因此执行方法后需要再次判断
     */
    @Override
    public Object createInstance(ApplicationContext context) {
        if (context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        FactoryBean<?> factoryBean = (FactoryBean<?>) context.registerBean(this.factoryBeanDefinition);
        if (context.contains(this.getBeanName())) {
            return context.getBean(this.getBeanName());
        }
        Object bean = factoryBean.getObject();
        return LogUtil.logIfDebugEnabled(log, log -> log.debug("instantiate bean from factory bean: {}", bean), bean);
    }

    public static FactoryBean<?> getFactoryBeanCache(BeanDefinition beanDefinition) {
        return SNAPSHOT_FACTORY_BEAN_CACHE.computeIfAbsent(beanDefinition.getBeanName(), k -> (FactoryBean<?>) newInstance(beanDefinition.getBeanType(), beanDefinition.getDefaultConstructArgs()));
    }

    public static Map<String, FactoryBean<?>> getFactoryBeanCacheMap() {
        return SNAPSHOT_FACTORY_BEAN_CACHE;
    }

    public static void addFactoryBeanCache(String beanName, FactoryBean<?> factoryBean) {
        SNAPSHOT_FACTORY_BEAN_CACHE.putIfAbsent(beanName, factoryBean);
    }
}
