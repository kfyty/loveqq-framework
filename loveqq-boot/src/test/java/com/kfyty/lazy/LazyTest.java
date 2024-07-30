package com.kfyty.lazy;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lazy;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Scope;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/10 19:56
 * @email kfyty725@hotmail.com
 */
@BootApplication
public class LazyTest implements CommandLineRunner {
    private static boolean configLazyF;
    private static boolean lazyBeanF;
    private static boolean scopeLazyBeanF;

    @Autowired
    private ConfigLazy configLazy;

    @Autowired
    private LazyBean lazyBean;

    @Autowired
    private ScopeLazyBean scopeLazyBean;

    @Autowired
    private ScopeLazyBean scopeLazyBean2;

    @Test
    public void test() {
        configLazyF = false;
        lazyBeanF = false;
        scopeLazyBeanF = false;
        K.run(LazyTest.class);
    }

    @Override
    public void run(String... args) throws Exception {
        Assertions.assertFalse(configLazyF);
        Assertions.assertFalse(lazyBeanF);
        Assertions.assertFalse(scopeLazyBeanF);
        Assertions.assertSame(this.configLazy.lazyBean(), this.lazyBean);
        Assertions.assertSame(this.scopeLazyBean, this.scopeLazyBean2);

        Assertions.assertNotEquals(this.scopeLazyBean.test(), this.scopeLazyBean.test());
        Assertions.assertTrue(scopeLazyBeanF);

        this.configLazy.test();
        Assertions.assertTrue(configLazyF);
        Assertions.assertFalse(lazyBeanF);

        this.lazyBean.test();
        Assertions.assertTrue(lazyBeanF);
    }

    @Lazy
    @Configuration
    public static class ConfigLazy {
        @Autowired
        private ScopeLazyBean scopeLazyBean;

        public ConfigLazy() {
            if (Objects.equals(this.getClass().getSimpleName(), "ConfigLazy")) {
                configLazyF = true;
            }
        }

        @Bean
        public LazyBean lazyBean() {
            return new LazyBean();
        }

        @Bean
        @Scope(BeanDefinition.SCOPE_PROTOTYPE)
        public ScopeLazyBean scopeLazyBean() {
            return new ScopeLazyBean();
        }

        public void test() {

        }
    }

    public static class ScopeLazyBean {
        private long time;

        @Autowired
        private ConfigLazy configLazy;

        public ScopeLazyBean() {
            if (Objects.equals(this.getClass().getSimpleName(), "ScopeLazyBean")) {
                time = System.nanoTime();
                scopeLazyBeanF = true;
            }
        }

        public long test() {
            return time;
        }
    }

    public static class LazyBean {
        public LazyBean() {
            if (Objects.equals(this.getClass().getSimpleName(), "LazyBean")) {
                lazyBeanF = true;
            }
        }

        public void test() {

        }
    }
}
