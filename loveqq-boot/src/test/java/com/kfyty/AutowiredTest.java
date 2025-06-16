package com.kfyty;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ImportBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Service;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.annotation.GetMapping;
import com.kfyty.loveqq.framework.web.core.annotation.PostMapping;
import com.kfyty.loveqq.framework.web.core.annotation.PutMapping;
import com.kfyty.loveqq.framework.web.core.annotation.RestController;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述: 自动注入测试
 *
 * @author kfyty725
 * @date 2021/6/14 22:17
 * @email kfyty725@hotmail.com
 */
@Slf4j
@RestController
@BootApplication
public class AutowiredTest {
    @Value("${id}")
    private Integer id;

    @Autowired
    private AutowiredTest autowiredTest;

    @Bean
    public Supplier<Bean1> b1() {
        return Bean1::new;
    }

    @Bean
    public Supplier<Bean2> b2() {
        return Bean2::new;
    }

    /**
     * {@link SimpleGeneric#getSimpleType()} 如果不忽略 {@link SimpleGeneric#IGNORED_NESTED_GENERIC_CLASSES}
     * 那么就会解析 {@link Bean2}，此时就会将 bean2 标记为解析中，那么创建 bean2 时由于还要再次注入 bean2 就会触发循环依赖。
     * 而如果忽略的话，就会注入正确的 b2，那么 bean2 就会在最后统一实例化的时候实例化，此时不会标记 bean2 解析中，则循环依赖可解决
     */
    @Autowired
    public void setB1B2(Supplier<Bean1> bean1Supplier, Supplier<Bean2> bean2Supplier) {
        Assertions.assertTrue(bean1Supplier.get() instanceof Bean1);
        Assertions.assertTrue(bean2Supplier.get() instanceof Bean2);
    }

    @Test
    @PostMapping("test")
    public void autowiredTest() {
        K.run(AutowiredTest.class);
    }

    @Bean
    public Bean1 bean1() {
        Assertions.assertNotNull(this.autowiredTest);
        return new Bean1();
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Method method = ReflectUtil.getMethod(this.autowiredTest.getClass(), "autowiredTest");
        Assertions.assertTrue(AnnotationUtil.hasAnnotation(this.autowiredTest, Component.class));
        Assertions.assertTrue(AnnotationUtil.hasAnnotation(this.autowiredTest, RestController.class));
        Assertions.assertTrue(AnnotationUtil.hasAnyAnnotation(this.autowiredTest, RestController.class, Component.class));
        Assertions.assertTrue(AnnotationUtil.hasAnyAnnotation(this.autowiredTest, Configuration.class, Component.class));
        Assertions.assertTrue(AnnotationUtil.hasAnnotation(method, PostMapping.class));
        Assertions.assertTrue(AnnotationUtil.hasAnyAnnotation(method, GetMapping.class, PostMapping.class));
        Assertions.assertFalse(AnnotationUtil.hasAnyAnnotation(method, GetMapping.class, PutMapping.class));
        Assertions.assertEquals(10, AnnotationUtil.findAnnotations(this.autowiredTest).length);
        Assertions.assertEquals(6, AnnotationUtil.findAnnotations(method).length);
    }
}

@Configuration
class Config {
    @Autowired
    private AutowiredTest test;

    @Autowired
    public Config(Factory factory, HelloInter helloInter, List<Inter> inters) {
        Assertions.assertNotNull(factory);
        Assertions.assertSame(helloInter.bean5(), helloInter.bean5());
        Assertions.assertEquals("kfyty", helloInter.hello("kfyty"));
    }

    @Bean
    public Bean2 bean2(Bean1 bean1, Bean3 bean3, Inter1 inter1, List<Inter> inters, Inter[] interArr) {
        Assertions.assertNotNull(test);
        Map<String, Inter> interMap = Arrays.stream(interArr).collect(Collectors.toMap(k -> BeanUtil.getBeanName(k.getClass()), Function.identity()));
        Assertions.assertSame(bean1, test.bean1());
        Assertions.assertSame(bean3, this.bean3(inter1, interMap));
        Assertions.assertSame(bean3, this.bean3(inter1, interMap));
        Assertions.assertEquals(2, inters.size());
        Assertions.assertEquals(inters, Arrays.stream(interArr).collect(Collectors.toList()));
        return new Bean2();
    }

    @Bean
    public Bean3 bean3(@Autowired("inter1") Inter inter1, Map<String, Inter> interMap) {
        Assertions.assertNotNull(test);
        return new Bean3();
    }
}

class Bean1 {
}

class Bean2 {
}

class Bean3 {
}

class Bean4 {
}

class Bean5 {
}

interface Inter {
}

abstract class InterImpl<T> implements Inter {
    @Autowired
    protected T t;

    @Resource
    public void setTT(T t) {
        Assertions.assertSame(this.t, t);
    }
}

@Service
class Inter1 extends InterImpl<Bean1> implements InitializingBean {
    @Autowired
    private Bean1 bean1;

    @Autowired
    private Bean4 bean4;

    @Bean
    public Bean4 bean4(Bean1 bean1) {
        Assertions.assertNotNull(this.t);
        Assertions.assertNotNull(this.bean1);
        return new Bean4();
    }

    @Override
    public void afterPropertiesSet() {
        Assertions.assertSame(bean1, this.t);
        Assertions.assertNotNull(bean4);
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
        Assertions.assertSame(bean2, this.t);
        Assertions.assertSame(self, this);
    }
}

interface Factory {
}

class FactoryImpl implements Factory {
}

@Component
class FactoryImport implements ImportBeanDefinition {

    @Override
    public Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return e -> !e.equals(Factory.class) && Factory.class.isAssignableFrom(e);
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        return genericBeanDefinition(FactoryProxy.class).addConstructorArgs(Class.class, clazz).getBeanDefinition();
    }
}

class FactoryProxy implements FactoryBean<Factory> {
    private final Class<?> clazz;

    public FactoryProxy() {
        this(Object.class);
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

interface HelloInter {

    String hello(String name);

    Bean5 bean5();
}

@Configuration
class HelloInterImpl implements HelloInter, InitializingBean {
    private boolean flag = false;

    @Autowired
    private Factory factory;

    @PostConstruct
    public void init() {
        flag = true;
    }

    @Override
    public String hello(String name) {
        return name;
    }

    @Bean
    @Override
    public Bean5 bean5() {
        Assertions.assertNotNull(factory);
        return new Bean5();
    }

    @Override
    public void afterPropertiesSet() {
        Assertions.assertNotNull(this.factory);
        Assertions.assertTrue(this.flag);
    }
}

interface ITestEvent {
    /**
     * jdk 代理下，事件监听器必须存在接口，否则请使用 cglib 代理
     */
    void onTestEvent(TestEvent testEvent);
}

@Component
@EventListener
class TestEventListener implements ITestEvent, ApplicationListener<TestEvent>, InitializingBean {
    @Autowired
    private ApplicationContext context;

    @Override
    public void onApplicationEvent(TestEvent testEvent) {
        Assertions.assertEquals(testEvent.getSource(), "event");
    }

    @Override
    @EventListener(TestEvent.class)
    public void onTestEvent(TestEvent testEvent) {
        Assertions.assertEquals(testEvent.getSource(), "event");
    }

    @Override
    public void afterPropertiesSet() {
        this.context.publishEvent(new TestEvent("event"));
    }
}

class TestEvent extends ApplicationEvent<String> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public TestEvent(String source) {
        super(source);
    }
}

class Entity {
}

interface Base<T, K> {
}

abstract class BaseImpl<T, K> implements Base<T, K> {
}

@Configuration
class DefaultBase extends BaseImpl<Entity, Integer> {

    @Bean
    public Base<Bean1, Long> base1() {
        return new Base<Bean1, Long>() {
        };
    }

    @Bean
    public Base<Bean2, Long> base2() {
        return new Base<Bean2, Long>() {
        };
    }
}

@Component
class CommonBase extends BaseImpl<Bean1, Integer> {
}

class BaseController<T, K> {
    @Autowired
    protected Base<T, K> service;

    @Autowired
    public void setServiceT(Base<T, K> service) {
        Assertions.assertSame(this.service, service);
    }
}

class IntBaseController<T> extends BaseController<T, Integer> {
}

@Component
class DefaultController extends IntBaseController<Entity> implements InitializingBean {
    @Autowired
    private DefaultBase defaultBase;

    @Autowired
    private Base<Bean1, Long> bean1Base;

    @Autowired
    private Base<Bean2, Long> bean2Base;

    @Override
    public void afterPropertiesSet() {
        Assertions.assertSame(this.defaultBase, this.service);
    }
}
