package com.kfyty.by;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.By;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/12/2 12:03
 * @email kfyty725@hotmail.com
 */
@BootApplication
public class DelegateTest implements CommandLineRunner {
    @Autowired
    private Sub sub;

    @Autowired
    private Delegate delegate;

    @Override
    public void run(String... args) throws Exception {
        this.sub.test(1);
        this.delegate.test(1);

        // 校验类加载器是否正常
        JarIndexClassLoader cl = (JarIndexClassLoader) Thread.currentThread().getContextClassLoader();
    }

    @Test
    public void test() {
        K.run(DelegateTest.class);
    }

    @Component
    static class Parent {

        private Integer test(int i) {
            return i + 1;
        }
    }

    @By
    @Component
    static class Sub extends Parent implements com.kfyty.loveqq.framework.core.autoconfig.delegate.Delegate, InitializingBean {

        @Override
        public void afterPropertiesSet() {
            this.test(1);
        }

        @By
        public Integer test(int i) {
            Integer superValue = (Integer) invoke();
            Integer superValue2 = (Integer) invoke(2);
            Integer superValue3 = (Integer) invoke(this, new Object[]{2});
            Assertions.assertEquals(superValue, i + 1);
            Assertions.assertEquals(superValue2, 3);
            Assertions.assertEquals(superValue3, 3);
            return i + 2;
        }
    }

    @By
    @Component
    static class Delegate implements com.kfyty.loveqq.framework.core.autoconfig.delegate.Delegate, InitializingBean {

        @Override
        public void afterPropertiesSet() {
            this.test(1);
        }

        @By(byName = "parent")
        public Integer test(int i) {
            Integer superValue = (Integer) invoke();
            Integer superValue2 = (Integer) invoke(2);
            Integer superValue3 = (Integer) invoke(new Parent(), new Object[]{2});
            Assertions.assertEquals(superValue, i + 1);
            Assertions.assertEquals(superValue2, 3);
            Assertions.assertEquals(superValue3, 3);
            return i + 2;
        }
    }
}
