package com.kfyty.property;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/7/24 14:05
 * @email kfyty725@hotmail.com
 */
@BootApplication
public class PropertyValueTest implements CommandLineRunner {
    @Autowired
    private PropertyValueBean propertyValueBean;

    @Test
    public void test() {
        K.run(PropertyValueTest.class);
    }

    @Override
    public void run(String... args) throws Exception {
        Assert.assertSame(this.propertyValueBean.getId(), 1);
    }

    @Getter
    static class PropertyValueBean {
        private int id;

        private User1 user1;

        private UserService userService;

        private List<UserService> userServices;

        private Map<String, UserService> userServiceMap;
    }

    @Component
    static class PropertyValueBeanConfig implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(BeanFactory beanFactory) {
            BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(PropertyValueBean.class)
                    .addPropertyValue("id", 1)
                    .addPropertyValue("user1", User1.class)
                    .addPropertyValue("userService", "userServiceImpl1", UserServiceImpl1.class)
                    .addPropertyValue("userServices", UserService.class)
                    .addPropertyValue("userServiceMap", UserService.class)
                    .getBeanDefinition();
            beanFactory.registerBeanDefinition(beanDefinition);
        }
    }

    interface UserService {}

    @Component
    static class User1 {}

    @Component
    static class UserServiceImpl1 implements UserService {}

    @Component
    static class UserServiceImpl2 implements UserService {}
}
