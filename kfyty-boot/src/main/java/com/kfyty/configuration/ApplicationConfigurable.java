package com.kfyty.configuration;

import com.kfyty.generate.GenerateSources;
import com.kfyty.generate.configuration.GenerateConfigurable;
import com.kfyty.jdbc.SqlSession;
import com.kfyty.mvc.handler.MVCAnnotationHandler;
import com.kfyty.support.configuration.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: 应用配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:33
 * @since JDK 1.8
 */
@Slf4j
public class ApplicationConfigurable {
    private GenerateConfigurable generateConfigurable;

    @Getter
    private Map<Class<?>, Object> beanResources;

    private ApplicationConfigurable() {

    }

    public static ApplicationConfigurable initApplicationConfigurable() throws Exception {
        ApplicationConfigurable applicationConfigurable = new ApplicationConfigurable();
        applicationConfigurable.beanResources = new HashMap<>();
        applicationConfigurable.beanResources.put(SqlSession.class, new SqlSession());
        applicationConfigurable.beanResources.put(GenerateSources.class, new GenerateSources());
        applicationConfigurable.beanResources.put(MVCAnnotationHandler.class, new MVCAnnotationHandler());
        return applicationConfigurable;
    }

    public void autoConfigurationAfterCheck() throws Exception {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean isAccessible = field.isAccessible();
            field.setAccessible(true);
            Object o = field.get(this);
            field.setAccessible(isAccessible);
            if(o != null && Configuration.class.isAssignableFrom(field.getType())) {
                o.getClass().getMethod("autoConfigurationAfterCheck").invoke(o);
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(": auto configuration after check success !");
        }
    }

    public GenerateConfigurable getGenerateConfigurable() {
        if(this.generateConfigurable != null) {
            return this.generateConfigurable;
        }
        if(!this.beanResources.containsKey(GenerateConfigurable.class)) {
            this.beanResources.put(GenerateConfigurable.class, (this.generateConfigurable = new GenerateConfigurable(true)));
        }
        return getGenerateConfigurable();
    }

    public void executeAutoGenerateSources() throws Exception {
        if(this.generateConfigurable == null || !this.generateConfigurable.isAutoConfiguration()) {
            return;
        }
        ((GenerateSources) this.beanResources.get(GenerateSources.class)).generate();
    }
}
