package com.kfyty.component;

import com.kfyty.boot.K;
import com.kfyty.support.autoconfig.CommandLineRunner;
import com.kfyty.support.autoconfig.InitializingBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.ComponentFilter;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Lookup;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.autoconfig.annotation.Scope;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/10 19:56
 * @email kfyty725@hotmail.com
 */
@BootApplication
public class ComponentTest implements CommandLineRunner {
    @Autowired
    private A a;

    @Autowired(required = false)
    private B b;

    @Autowired
    private C c;

    @Autowired
    private ComponentT componentT1;

    @Autowired
    private ComponentT componentT2;

    @Test
    public void test() {
        K.run(ComponentTest.class);
    }

    @Override
    public void run(String... args) throws Exception {
        Assert.assertNotNull(a);
        Assert.assertNull(b);
        Assert.assertNotNull(c);
        Assert.assertNotSame(componentT1, componentT2);
    }
}

class ComponentT {}
class ComponentS {
    @Getter
    private final long time;

    public ComponentS() {
        this.time = System.currentTimeMillis();
    }
}

@Component
@ComponentScan(excludeFilter = @ComponentFilter(classes = B.class))
abstract class A implements InitializingBean, CommandLineRunner {
    @Autowired
    private ComponentS componentS1;

    @Autowired
    private ComponentS componentS2;

    @Autowired
    private Map<String, ComponentS> componentS;

    @Lookup
    public abstract C c();

    @Lookup
    public abstract List<C> cList();

    @Override
    public void afterPropertiesSet() {
        Assert.assertNotNull(this.componentS1);
        Assert.assertNotNull(this.componentS2);
        Assert.assertSame(this.componentS1, this.componentS2);
        Assert.assertNotEquals(this.componentS1.getTime(), this.componentS2.getTime());
        Assert.assertEquals(1, this.componentS.size());
    }

    @Override
    public void run(String... args) throws Exception {
        C c1 = this.c();
        C c2 = this.c();
        List<C> cs = this.cList();
        Assert.assertNotNull(c1);
        Assert.assertNotNull(c2);
        Assert.assertNotEquals(c1, c2);
        Assert.assertEquals(1, cs.size());
    }
}

@Component
abstract class LookupBean {
    @Lookup
    public abstract B b();
}

@Component
@ComponentScan(excludeFilter = @ComponentFilter(classes = C.class))
class B {}

@Configuration
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class C {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public ComponentT componentT() {
        return new ComponentT();
    }

    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE, scopeProxy = true)
    public ComponentS componentScope() {
        return new ComponentS();
    }
}

abstract class AA {}

@Order(0)
@Component
class AAConfig {
    @Autowired
    private List<AA> aas;
}

@Order(2)
@Component
class AA1 extends AA {
    @Autowired
    private AA2 aa2;
}

@Order(3)
@Component
class AA2 extends AA {
    @Autowired
    private AA1 aa1;
}
