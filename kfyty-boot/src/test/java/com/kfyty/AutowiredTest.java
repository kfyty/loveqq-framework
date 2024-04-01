package com.kfyty;

import com.kfyty.boot.K;
import com.kfyty.boot.web.WebMvcAutoConfigListener;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ImportBeanDefinition;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.core.autoconfig.annotation.ComponentScan;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.EventListener;
import com.kfyty.core.autoconfig.annotation.Service;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.event.ApplicationEvent;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.web.mvc.core.annotation.GetMapping;
import com.kfyty.web.mvc.core.annotation.PostMapping;
import com.kfyty.web.mvc.core.annotation.PutMapping;
import com.kfyty.web.mvc.core.annotation.RestController;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kfyty.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

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
@ComponentScan(excludeFilter = @ComponentFilter(classes = WebMvcAutoConfigListener.class))
public class AutowiredTest {
    @Value("${id}")
    private Integer id;

    @Autowired
    private AutowiredTest  autowiredTest;

    @Test
    @PostMapping("test")
    public void autowiredTest() {
        K.run(AutowiredTest.class);
    }

    @Bean
    public Bean1 bean1() {
        Assert.assertNotNull(this.autowiredTest);
        return new Bean1();
    }

    @PostConstruct
    public void afterPropertiesSet() {
        Method method = ReflectUtil.getMethod(this.autowiredTest.getClass(), "autowiredTest");
        Assert.assertTrue(AnnotationUtil.hasAnnotationElement(this.autowiredTest, Component.class));
        Assert.assertTrue(AnnotationUtil.hasAnnotation(this.autowiredTest, RestController.class));
        Assert.assertTrue(AnnotationUtil.hasAnyAnnotation(this.autowiredTest, RestController.class, Component.class));
        Assert.assertFalse(AnnotationUtil.hasAnyAnnotation(this.autowiredTest, Configuration.class, Component.class));
        Assert.assertTrue(AnnotationUtil.hasAnnotation(method, PostMapping.class));
        Assert.assertTrue(AnnotationUtil.hasAnyAnnotation(method, GetMapping.class, PostMapping.class));
        Assert.assertFalse(AnnotationUtil.hasAnyAnnotation(method, GetMapping.class, PutMapping.class));
        Assert.assertEquals(3, AnnotationUtil.findAnnotations(this.autowiredTest).length);
        Assert.assertEquals(2, AnnotationUtil.findAnnotations(method).length);
    }
}

@Configuration
class Config {
    @Autowired
    private AutowiredTest test;

    @Autowired
    public Config(Factory factory, HelloInter helloInter, List<Inter> inters) {
        Assert.assertNotNull(factory);
        Assert.assertSame(helloInter.bean5(), helloInter.bean5());
        Assert.assertEquals("kfyty", helloInter.hello("kfyty"));
    }

    @Bean
    public Bean2 bean2(Bean1 bean1, Bean3 bean3, Inter1 inter1, List<Inter> inters, Inter[] interArr) {
        Assert.assertNotNull(test);
        Map<String, Inter> interMap = Arrays.stream(interArr).collect(Collectors.toMap(k -> BeanUtil.getBeanName(k.getClass()), Function.identity()));
        Assert.assertSame(bean1, test.bean1());
        Assert.assertSame(bean3, this.bean3(inter1, interMap));
        Assert.assertSame(bean3, this.bean3(inter1, interMap));
        Assert.assertEquals(2, inters.size());
        Assert.assertEquals(inters, Arrays.stream(interArr).collect(Collectors.toList()));
        return new Bean2();
    }

    @Bean
    public Bean3 bean3(@Autowired("inter1") Inter inter1, Map<String, Inter> interMap) {
        Assert.assertNotNull(test);
        return new Bean3();
    }
}

class Bean1 {}

class Bean2 {}

class Bean3 {}

class Bean4 {}

class Bean5 {}

interface Inter {}

abstract class InterImpl<T> implements Inter {
    @Autowired
    protected T t;

    @Resource
    public void setTT(T t) {
        Assert.assertSame(this.t, t);
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
        Assert.assertNotNull(this.t);
        Assert.assertNotNull(this.bean1);
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

    @Bean
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
        Assert.assertNotNull(factory);
        return new Bean5();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.assertNotNull(this.factory);
        Assert.assertTrue(this.flag);
    }
}

interface ITestEvent {
    /**
     * jdk 代理下，事件监听器必须存在接口，否则请使用 cglib 代理
     */
    void onTestEvent(TestEvent testEvent);
}

@Component
class TestEventListener implements ITestEvent, ApplicationListener<TestEvent>, InitializingBean {
    @Autowired
    private ApplicationContext context;

    @Override
    public void onApplicationEvent(TestEvent testEvent) {
        Assert.assertEquals(testEvent.getSource(), "event");
    }

    @Override
    @EventListener(TestEvent.class)
    public void onTestEvent(TestEvent testEvent) {
        Assert.assertEquals(testEvent.getSource(), "event");
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

class Entity {}
interface Base<T, K> {}
abstract class BaseImpl<T, K> implements Base<T, K> {}

@Configuration
class DefaultBase extends BaseImpl<Entity, Integer> {

    @Bean
    public Base<Bean1, Long> base1() {
        return new Base<Bean1, Long>() {};
    }

    @Bean
    public Base<Bean2, Long> base2() {
        return new Base<Bean2, Long>() {};
    }
}

@Component
class CommonBase extends BaseImpl<Bean1, Integer> {}

class BaseController<T, K> {
    @Autowired
    protected Base<T, K> service;

    @Autowired
    public void setServiceT(Base<T, K> service) {
        Assert.assertSame(this.service, service);
    }
}

class IntBaseController<T> extends BaseController<T, Integer> {}

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
        Assert.assertSame(this.defaultBase, this.service);
    }
}
