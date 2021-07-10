package com.kfyty.component;

import com.kfyty.boot.K;
import com.kfyty.support.autoconfig.CommandLineRunner;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.BootApplication;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.ComponentFilter;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import org.junit.Assert;
import org.junit.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/7/10 19:56
 * @email kfyty725@hotmail.com
 */
@BootApplication
public class ComponentFilterTest implements CommandLineRunner {
    @Autowired(required = false)
    private A a;

    @Autowired(required = false)
    private B b;

    @Autowired(required = false)
    private C c;

    @Test
    public void test() {
        K.run(ComponentFilterTest.class);
    }

    @Override
    public void run(String... args) throws Exception {
        Assert.assertNotNull(a);
        Assert.assertNull(b);
        Assert.assertNotNull(c);
    }
}

@Component
@ComponentScan(excludeFilter = @ComponentFilter(classes = B.class))
class A {}

@Component
@ComponentScan(excludeFilter = @ComponentFilter(classes = C.class))
class B {}

@Component
class C {}
