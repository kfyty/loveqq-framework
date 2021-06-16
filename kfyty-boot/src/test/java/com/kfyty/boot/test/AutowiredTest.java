package com.kfyty.boot.test;

import com.kfyty.boot.K;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.support.autoconfig.annotation.Service;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.FactoryBean;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述: 自动注入测试
 *
 * @author kfyty725
 * @date 2021/6/14 22:17
 * @email kfyty725@hotmail.com
 */
@Slf4j
@BootApplication
public class AutowiredTest {

    @Test
    public void autowiredTest() {
        K.run(AutowiredTest.class);
    }

    @Bean
    public Bean1 bean1() {
        return new Bean1();
    }
}

@Configuration
@NoArgsConstructor
class Config {
    @Autowired
    private AutowiredTest test;

    @Autowired
    public Config(Factory factory, List<Inter> inters) {
        Assert.assertNotNull(factory);
    }

    @Bean
    public Bean2 bean2(Bean1 bean1, Bean3 bean3, Inter1 inter1, List<Inter> inters, Inter[] interArr) {
        Map<String, Inter> interMap = Arrays.stream(interArr).collect(Collectors.toMap(k -> BeanUtil.convert2BeanName(k.getClass()), Function.identity()));
        Assert.assertSame(bean1, test.bean1());
        Assert.assertSame(bean3, this.bean3(inter1, interMap));
        Assert.assertEquals(2, inters.size());
        return new Bean2();
    }

    @Bean
    public Bean3 bean3(@Qualifier("inter1") Inter inter1, Map<String, Inter> interMap) {
        return new Bean3();
    }
}

class Bean1 {}

class Bean2 {}

class Bean3 {}

class Bean4 {}

interface Inter {}

abstract class InterImpl<T> implements Inter {
    @Autowired
    protected T t;
}

@Service
class Inter1 extends InterImpl<Bean1> implements InitializingBean {
    @Autowired
    private Bean1 bean1;

    @Autowired
    private Bean4 bean4;

    @Bean
    public Bean4 bean4(Bean1 bean1) {
        return new Bean4();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.assertSame(bean1, this.t);
        Assert.assertNotNull(bean4);
    }
}

@Service
class Inter2 extends InterImpl<Bean2> implements InitializingBean {
    @Autowired
    private Bean2 bean2;

    @Autowired
    private Inter2 self;

    @Override
    public void afterPropertiesSet() {
        Assert.assertSame(bean2, this.t);
        Assert.assertSame(self, this);
    }
}

interface Factory {}

class FactoryImpl implements Factory {}

@Component
class FactoryImport implements ImportBeanDefine {

    @Override
    public Set<BeanDefinition> doImport(Set<Class<?>> scanClasses) {
        return scanClasses
                .stream()
                .filter(e -> !e.equals(Factory.class) && Factory.class.isAssignableFrom(e))
                .map(e -> GenericBeanDefinition.from(FactoryProxy.class).addConstructorArgs(Class.class, e))
                .collect(Collectors.toSet());
    }
}

class FactoryProxy implements FactoryBean<Factory> {
    private final Class<?> clazz;

    public FactoryProxy() {
        this(null);
    }

    public FactoryProxy(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getBeanType() {
        return this.clazz;
    }

    @Override
    public Factory getObject() {
        return (Factory) ReflectUtil.newInstance(clazz);
    }
}
