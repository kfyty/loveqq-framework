package com.kfyty.component;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lazy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lookup;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Scope;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.support.Pair;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertNotNull(a);
        Assertions.assertNull(b);
        Assertions.assertNotNull(c);
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

enum PEnum {
    P1, P2;
}

@Data
@Component
@ConfigurationProperties("k.ref")
class PropertiesRefConfig implements InitializingBean {
    private PropertiesConfig.User object;

    private List<PropertiesConfig.User> arr;

    @NestedConfigurationProperty
    private Nested nested;

    @Override
    public void afterPropertiesSet() {
        Assertions.assertEquals(object.getId(), "1");
        Assertions.assertEquals(object.getName(), "test");
        Assertions.assertEquals(arr.size(), 2);
        Assertions.assertEquals(arr.get(0).getId(), "2");
        Assertions.assertEquals(arr.get(0).getName(), "test2");

        Assertions.assertEquals(nested.getObject(), object);
        Assertions.assertEquals(nested.getArr().get(1), arr.get(1));
        Assertions.assertEquals(nested.getArr().get(0).getChildren().get(0).getId(), "3");
        Assertions.assertEquals(nested.getArr().get(0).getChildren().get(0).getName(), "test3");
    }

    @Data
    public static class Nested {
        private PropertiesConfig.User object;

        private List<PropertiesConfig.User> arr;
    }
}

@Component
@ConfigurationProperties("k.prop")
@ConditionalOnProperty(value = "k.prop.enable", havingValue = "true")
class PropertiesConfig implements InitializingBean {
    @Value("http://${k.prop.ip}:${port:8080}/${${index.path:index}:}")
    private String url;

    private PEnum pe;

    private Properties opt;

    private Integer[] idArr;

    private List<Integer> ids;

    private List<User> users;

    private Map<String, User> userMap;

    private List<List<User>> configLists;

    private Map<String, User>[] configMapArr;

    private Map<String, User[]> configMapNestedArr;

    private List<Map<String, User>> listConfigMap;

    private Map<String, Map<String, User>> configMapMap;

    private Map<String, List<User>> configListMap;

    @NestedConfigurationProperty
    private User user;

    private com.kfyty.loveqq.framework.core.lang.Value<User> valueUser;

    private com.kfyty.loveqq.framework.core.lang.Value<List<User>> valueListUser;

    private com.kfyty.loveqq.framework.core.lang.Value<Map<String, User>> valueMapUser;

    private com.kfyty.loveqq.framework.core.lang.Value<List<String>> values;

    private Pair<Long, String> pair;

    private Pair<String, List<String>> listPair;

    public PropertiesConfig(@Value("${k.prop.ip}") String ip) {
        Assertions.assertEquals(ip, "127.0.0.1");
    }

    @Override
    public void afterPropertiesSet() {
        Assertions.assertEquals(this.pe, PEnum.P1);
        Assertions.assertEquals(this.url, "http://127.0.0.1:8080/");
        Assertions.assertEquals(this.pair.getKey(), Long.valueOf(1L));
        Assertions.assertEquals(this.pair.getValue(), "name_of_1");
        Assertions.assertArrayEquals(this.idArr, new Integer[]{1, 2});
        Assertions.assertEquals(this.ids, Arrays.asList(1, 2));
        Assertions.assertEquals(this.users.size(), 2);
        Assertions.assertEquals(this.user.getId(), "1");
        Assertions.assertEquals(this.user.getName(), "name");
        Assertions.assertEquals(this.users.get(1).getName(), "name2");
        Assertions.assertEquals(this.userMap.get("1").getChildren().get(0).getExtra().get(0), "map");
        Assertions.assertEquals(this.opt.getProperty("user.enable"), "true");
        Assertions.assertEquals(this.configMapArr[0].get("map").getId(), "arr_list_map");
        Assertions.assertEquals(this.configMapNestedArr.get("map")[0].getId(), "map_nested_arr");
        Assertions.assertEquals(this.configLists.get(0).get(0).getId(), "unique_list_list");
        Assertions.assertEquals(this.listConfigMap.get(0).get("map").getId(), "unique_list_map");
        Assertions.assertEquals(this.configMapMap.get("map").get("nested").getId(), "unique_map_map");
        Assertions.assertEquals(this.configListMap.get("list").get(0).getId(), "unique_map_list");
        Assertions.assertEquals(this.values.get(), Arrays.asList("1", "2"));
        Assertions.assertEquals(this.valueUser.get().getId(), "id");
        Assertions.assertEquals(this.valueListUser.get().get(0).getId(), "id");
        Assertions.assertEquals(this.valueMapUser.get().get("1").getId(), "id");
        Assertions.assertEquals(this.listPair.getValue(), Arrays.asList("1", "2"));
    }

    @Data
    @NestedConfigurationProperty
    public static class User {
        private String id;

        private String name;

        private List<String> extra;

        private List<User> children;
    }
}

@Lookup
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
        Assertions.assertNotNull(this.componentS1);
        Assertions.assertNotNull(this.componentS2);
        Assertions.assertSame(this.componentS1, this.componentS2);
        Assertions.assertNotEquals(this.componentS1.getTime(), this.componentS2.getTime());
        Assertions.assertEquals(1, this.componentS.size());
    }

    @Override
    public void run(String... args) throws Exception {
        C c1 = this.c();
        C c2 = this.c();
        List<C> cs = this.cList();
        Assertions.assertNotNull(c1);
        Assertions.assertNotNull(c2);
        Assertions.assertEquals(c1, c2);
        Assertions.assertEquals(1, cs.size());
    }
}

@Lookup
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
    private PrototypeDDD ddd;

    @Autowired
    private PrototypeDDD ddd2;

    @Autowired
    private ProtoTypeDDDFactory protoTypeDDDFactory;

    @Autowired
    private ProtoTypeDDDFactory protoTypeDDDFactory2;

    @Autowired
    private LazyAAA lazyAAA;

    @Override
    public void run(String... args) throws Exception {
        Assertions.assertNotEquals(this.lazyAAA.get(), this.lazyAAA.get());
        Assertions.assertSame(this.aaa, this.aaa2);
        Assertions.assertNotEquals(this.aaa.getTime(), this.aaa2.getTime());
        Assertions.assertSame(this.bbb, this.bbb2);
        Assertions.assertSame(this.aaa.getBBB(), this.bbb);
        Assertions.assertNotSame(this.ccc1, this.ccc2);
        Assertions.assertEquals(this.ccc1.getTime(), this.ccc1.getTime());
        Assertions.assertSame(this.protoTypeDDDFactory, this.protoTypeDDDFactory2);
        Assertions.assertSame(this.ddd, this.ddd2);
        Assertions.assertNotEquals(this.ddd.getTime(), this.ddd2.getTime());
        Assertions.assertNotEquals(this.aaa2.getBBB().getTime(), this.bbb2.getTime());
    }

    @Lookup
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

    public static class PrototypeDDD {
        private final long time = System.nanoTime();

        public long getTime() {
            return time;
        }
    }

    @Component
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
    public static class ProtoTypeDDDFactory implements FactoryBean<PrototypeDDD> {

        @Override
        public Class<?> getBeanType() {
            return PrototypeDDD.class;
        }

        @Override
        public PrototypeDDD getObject() {
            return new PrototypeDDD();
        }
    }

    @Lazy
    @Lookup
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
