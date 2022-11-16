package com.kfyty.component;

import com.kfyty.boot.K;
import com.kfyty.core.autoconfig.CommandLineRunner;
import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.core.autoconfig.annotation.ComponentScan;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.annotation.Lazy;
import com.kfyty.core.autoconfig.annotation.Lookup;
import com.kfyty.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.annotation.Scope;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnProperty;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

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

    @Test
    public void test() {
        K.run(ComponentTest.class);
    }

    @Override
    public void run(String... args) throws Exception {
        Assert.assertNotNull(a);
        Assert.assertNull(b);
        Assert.assertNotNull(c);
    }
}

class ComponentS {
    private static long time;

    public ComponentS() {
        if (this.getClass().getSimpleName().equals("ComponentS")) {
            System.out.println("ComponentS");
        }
    }

    public long getTime() {
        return time++;
    }
}

@Component
@ConfigurationProperties("k.prop")
@ConditionalOnProperty(value = "k.prop.enable", havingValue = "true")
class PropertiesConfig implements InitializingBean {
    @Value("http://${k.prop.ip}:${port:8080}/${${index.path:index}:}")
    private String url;

    private Properties opt;

    private List<Integer> ids;

    private List<User> users;

    private Map<String, User> userMap;

    private List<List<User>> configLists;

    private List<Map<String, User>> listConfigMap;

    private Map<String, Map<String, User>> configMapMap;

    private Map<String, List<User>> configListMap;

    @NestedConfigurationProperty
    private User user;

    @Override
    public void afterPropertiesSet() {
        Assert.assertEquals(this.url, "http://127.0.0.1:8080/");
        Assert.assertEquals(this.ids, Arrays.asList(1, 2));
        Assert.assertEquals(this.users.size(), 2);
        Assert.assertEquals(this.user.getId(), "1");
        Assert.assertEquals(this.user.getName(), "name");
        Assert.assertEquals(this.users.get(1).getName(), "name2");
        Assert.assertEquals(this.userMap.get("1").getChildren().get(0).getExtra().get(0), "map");
        Assert.assertEquals(this.opt.getProperty("user.enable"), "true");
        Assert.assertEquals(this.configLists.get(0).get(0).getId(), "unique_list_list");
        Assert.assertEquals(this.listConfigMap.get(0).get("map").getId(), "unique_list_map");
        Assert.assertEquals(this.configMapMap.get("map").get("nested").getId(), "unique_map_map");
        Assert.assertEquals(this.configListMap.get("list").get(0).getId(), "unique_map_list");
    }

    @Data
    public static class User {
        private String id;

        private String name;

        private List<String> extra;

        private List<User> children;
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
        Assert.assertEquals(c1, c2);
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
class B {
}

@Configuration
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class C {

    public C() {
        if (this.getClass().getSimpleName().equals("C")) {
            System.out.println("C");
        }
    }

    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
    public ComponentS componentScope() {
        return new ComponentS();
    }
}

abstract class AA {
}

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

@Component
class ComplexBean implements CommandLineRunner {
    @Autowired
    private PrototypeAAA aaa;

    @Autowired
    private PrototypeAAA aaa2;

    @Autowired
    private PrototypeBBB bbb;

    @Autowired
    private PrototypeBBB bbb2;

    @Autowired
    private PrototypeCCC ccc1;

    @Autowired
    private PrototypeCCC ccc2;

    @Autowired
    private LazyAAA lazyAAA;

    @Override
    public void run(String... args) throws Exception {
        Assert.assertNotEquals(this.lazyAAA.get(), this.lazyAAA.get());
        Assert.assertSame(this.aaa, this.aaa2);
        Assert.assertNotEquals(this.aaa.getTime(), this.aaa2.getTime());
        Assert.assertSame(this.bbb, this.bbb2);
        Assert.assertSame(this.aaa.getBBB(), this.bbb);
        Assert.assertNotSame(this.ccc1, this.ccc2);
        Assert.assertEquals(this.ccc1.getTime(), this.ccc1.getTime());
        Assert.assertNotEquals(this.aaa2.getBBB().getTime(), this.bbb2.getTime());
    }

    @Component
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public static abstract class PrototypeAAA {
        private final long time = System.nanoTime();

        @Lookup
        public abstract PrototypeBBB getBBB();

        public long getTime() {
            return time;
        }
    }

    @Component
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public static class PrototypeBBB {
        private final long time = System.nanoTime();

        public long getTime() {
            return time;
        }
    }

    @Component
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE, scopeProxy = false)
    public static class PrototypeCCC {
        private final long time = System.nanoTime();

        public long getTime() {
            return time;
        }
    }

    @Lazy
    @Component
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public static class LazyAAA {

        public LazyAAA() {
            if (this.getClass().getSimpleName().equals("LazyAAA")) {
                System.out.println("LazyAAA");
            }
        }

        @Lookup
        public PrototypeBBB getBBB() {
            return null;
        }

        public String get() {
            return UUID.randomUUID().toString();
        }
    }
}
