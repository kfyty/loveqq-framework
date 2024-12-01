package com.kfyty.by;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.OverrideBy;
import com.kfyty.loveqq.framework.core.autoconfig.delegate.By;
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
public class ByTest implements CommandLineRunner {
    @Autowired
    private Sub sub;

    @Autowired
    private Delegate delegate;

    @Override
    public void run(String... args) throws Exception {
        this.sub.test(1);
        this.delegate.test(1);
    }

    @Test
    public void test() {
        K.run(ByTest.class);
    }

    @Component
    static class Parent {

        private Integer test(int i) {
            return i + 1;
        }
    }

    @Component
    @OverrideBy
    static class Sub extends Parent implements By, InitializingBean {

        @Override
        public void afterPropertiesSet() {
            this.test(1);
        }

        @OverrideBy
        public Integer test(int i) {
            Integer superValue = (Integer) invokeSuper();
            Integer superValue2 = (Integer) invokeSuper(this, 2);
            Assertions.assertEquals(superValue, i + 1);
            Assertions.assertEquals(superValue2, 3);
            return i + 2;
        }
    }

    @Component
    @OverrideBy
    static class Delegate implements By, InitializingBean {

        @Override
        public void afterPropertiesSet() {
            this.test(1);
        }

        @OverrideBy(byName = "parent")
        public Integer test(int i) {
            Integer superValue = (Integer) invokeSuper();
            Integer superValue2 = (Integer) invokeSuper(new Parent(), 2);
            Assertions.assertEquals(superValue, i + 1);
            Assertions.assertEquals(superValue2, 3);
            return i + 2;
        }
    }
}
