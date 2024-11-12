package com.kfyty.generic;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/9/26 13:38
 * @email kfyty725@hotmail.com
 */
@BootApplication
public class GenericTest implements CommandLineRunner {
    @Autowired
    private WxCallbackContext wxCallbackContext;

    @Autowired
    private Map<String, Function> fun0;

    @Autowired
    private Map<String, Function<?, ?>> fun1;

    @Autowired
    private Map<String, Function<?, String>> fun2;

    @Override
    public void run(String... args) throws Exception {
        Assertions.assertEquals(wxCallbackContext.socialEventCallbacks.size(), 2);
        Assertions.assertEquals(wxCallbackContext.socialEventCallbackMap.size(), 2);
        Assertions.assertEquals(fun0.size(), 2);
        Assertions.assertEquals(fun1.size(), 2);
        Assertions.assertEquals(fun2.size(), 2);
    }

    @Test
    public void test() {
        K.run(GenericTest.class);
    }

    @Autowired
    public void test0(Function<Integer, String> f1, Function<Long, String> f2) {
        Assertions.assertEquals(f1.apply(1), "1");
        Assertions.assertEquals(f2.apply(1L), "1L");
    }

    @Bean
    public Function<Integer, String> test1() {
        return String::valueOf;
    }

    @Bean
    public Function<Long, String> test2() {
        return i -> String.valueOf(i) + 'L';
    }

    interface SocialEventAware<E extends SocialEventAware<E>> {}
    interface SocialEventCallback<E extends SocialEventAware<E>, S> {}

    static class WxEvent implements SocialEventAware<WxEvent> {}
    static class FsEvent implements SocialEventAware<FsEvent> {}

    @Component
    static class WxUserCallback implements SocialEventCallback<WxEvent, String> {}

    @Component
    static class WxDeptCallback implements SocialEventCallback<WxEvent, String> {}

    @Component
    static class FsUserCallback implements SocialEventCallback<FsEvent, Integer> {}

    @Component
    static class FsDeptCallback implements SocialEventCallback<FsEvent, Integer> {}

    abstract static class AbstractCallbackContext<E extends SocialEventAware<E>, S> {
        @Autowired
        protected List<SocialEventCallback<E, S>> socialEventCallbacks;

        @Autowired
        protected Map<String, SocialEventCallback<E, S>> socialEventCallbackMap;
    }

    @Component
    static class WxCallbackContext extends AbstractCallbackContext<WxEvent, String> {}

    @Component
    static class FsCallbackContext extends AbstractCallbackContext<FsEvent, Integer> {}

    static class AnyBean {}

    static class ParentConfig {

        @Bean
        public AnyBean anyBean() {
            return new AnyBean();
        }
    }

    @Configuration
    static class SubConfig extends ParentConfig implements InitializingBean {

        @Override
        public void afterPropertiesSet() {
//            Assertions.assertSame(this.anyBean(), this.anyBean());
        }
    }
}
