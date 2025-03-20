package com.kfyty.condition;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * 描述: 条件注解测试
 *
 * @author kfyty725
 * @date 2022/4/23 11:06
 * @email kfyty725@hotmail.com
 */
@Component
@EventListener
public class ConditionTest implements CommandLineRunner {
    private boolean isOverride;

    @Autowired(required = false)
    private II1 ii1;

    @Autowired(required = false)
    private II2 ii2;

    @Autowired(required = false)
    private II3 ii3;

    @Autowired(required = false)
    private II4 ii4;

    @Autowired(required = false)
    private List<Inter> cons;

    @Autowired
    private ApplicationContext context;

    @Bean
    public BB bbOverride() {
        this.isOverride = true;
        return new BB();
    }

    @EventListener
    public void onComplete(ContextRefreshedEvent event) {
        Assertions.assertSame(this.isOverride, true);
        Assertions.assertSame(this.cons.size(), 5);

        Assertions.assertNotNull(ii1);
        Assertions.assertNull(ii2);
        Assertions.assertNull(ii3);
        Assertions.assertNotNull(ii4);
    }

    @EventListener(condition = "arg0.source == 1")
    public void onInt1(IntEvent event) {
        Assertions.assertSame(event.getSource(), 1);
    }

    @EventListener(condition = "arg0.source == 2")
    public void onInt2(IntEvent event) {
        Assertions.assertSame(event.getSource(), 2);
    }

    @Override
    public void run(String... args) throws Exception {
        this.context.publishEvent(new IntEvent(1));
        this.context.publishEvent(new IntEvent(2));
    }

    public static class IntEvent extends ApplicationEvent<Integer> {
        /**
         * Constructs a prototypical Event.
         *
         * @param source The object on which the Event initially occurred.
         * @throws IllegalArgumentException if source is null.
         */
        public IntEvent(Integer source) {
            super(source);
        }
    }
}

interface Inter {}

@Component
@ConditionalOnBean({CC.class, BB.class})
@ConditionalOnClass("com.kfyty.condition.ConditionTest")
class AA implements Inter {}

@Component
@ConditionalOnMissingBean(BB.class)
class BB implements Inter {}

@Component
@ConditionalOnBean(BB.class)
class CC implements Inter {

    @Bean
    @ConditionalOnBean(AA.class)
    public EE ee() {
        return new EE();
    }
}

class DD implements Inter {}

@Component
@ConditionalOnMissingBean
class DDF implements FactoryBean<DD> {

    @Override
    public Class<?> getBeanType() {
        return DD.class;
    }

    @Override
    public DD getObject() {
        return new DD();
    }
}

class EE implements Inter {}

@Component
class GG1 {}
@Component
class HH1 {}
@Component
@ConditionalOnBean({GG1.class, HH1.class})
class II1 {}        // 应该存在

@Component
class GG2 {}
class HH2 {}
class HH3 {}
@Component
@ConditionalOnBean({GG2.class, HH2.class})
class II2 {}        // 不应该存在
@Component
@ConditionalOnMissingBean({GG2.class, HH2.class})
class II3 {}        // 不应该存在
@Component
@ConditionalOnMissingBean({HH2.class, HH3.class})
class II4 {}        // 应该存在