package com.kfyty.loveqq.framework.boot.dubbo.autoconfig;

import com.kfyty.loveqq.framework.boot.dubbo.autoconfig.annotation.DubboComponent;
import com.kfyty.loveqq.framework.boot.dubbo.autoconfig.annotation.DubboScan;
import com.kfyty.loveqq.framework.boot.dubbo.autoconfig.override.LoveqqReferenceConfig;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.beans.MethodBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述: dubbo 接口注册器
 * <p>
 * {@link DubboScan} 配置的扫描包会自动扫描，因此这里仅处理标记了 {@link DubboService} 的 bean 即可
 *
 * @author kfyty725
 * @date 2024/10/29 20:31
 * @email kfyty725@hotmail.com
 */
@Component
public class DubboServiceRegistry implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        for (Map.Entry<String, BeanDefinition> entry : beanFactory.getBeanDefinitions().entrySet()) {
            BeanDefinition beanDefinition = entry.getValue();
            if (!beanDefinition.isAutowireCandidate()) {
                continue;
            }
            DubboComponent dubboComponent = AnnotationUtil.findAnnotation(beanDefinition.getBeanType(), DubboComponent.class);
            AnnotatedElement annotatedElement = beanDefinition instanceof MethodBeanDefinition ? beanDefinition.getBeanMethod() : beanDefinition.getBeanType();
            if (dubboComponent != null) {
                this.resolveDubboReference(beanFactory, beanDefinition);
            }
            if (!ServiceConfig.class.isAssignableFrom(beanDefinition.getBeanType())) {
                DubboService dubboService = AnnotationUtil.findAnnotation(annotatedElement, DubboService.class);
                if (dubboService != null) {
                    List<BeanDefinition> beanDefinitions = this.buildDubboService(beanDefinition.getBeanType(), beanFactory.getBean(entry.getKey(), true), dubboService);
                    beanDefinitions.forEach(bd -> beanFactory.registerBeanDefinition(bd, false));
                }
            }
            if (!ReferenceConfig.class.isAssignableFrom(beanDefinition.getBeanType())) {
                DubboReference dubboReference = AnnotationUtil.findAnnotation(annotatedElement, DubboReference.class);
                if (dubboReference != null) {
                    beanFactory.registerBeanDefinition(this.buildDubboReference(beanDefinition.getBeanType(), dubboReference), false);
                }
            }
        }
    }

    protected List<BeanDefinition> buildDubboService(Class<?> clazz, Object ref, DubboService dubboService) {
        List<BeanDefinition> beanDefinitions = new LinkedList<>();
        for (Class<?> interfaceClass : ensureDubboServiceInterfaceClass(clazz)) {
            String serviceBeanName = generateServiceBeanName(dubboService, interfaceClass);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(serviceBeanName, ServiceConfig.class)
                    .addPropertyValue("ref", ref)
                    .addPropertyValue("interfaceName", interfaceClass.getName())
                    .addPropertyValue("interfaceClass", (Object) interfaceClass)
                    .addPropertyValue("interfaceClassLoader", interfaceClass.getClassLoader());
            beanDefinitions.add(builder.getBeanDefinition());
        }
        return beanDefinitions;
    }

    protected BeanDefinition buildDubboReference(Class<?> interfaceClass, DubboReference dubboReference) {
        String serviceBeanName = generateReferenceBeanName(dubboReference, interfaceClass);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(serviceBeanName, LoveqqReferenceConfig.class)
                .addConstructorArgs(Class.class, interfaceClass)
                .addPropertyValue("interfaceName", interfaceClass.getName())
                .addPropertyValue("interfaceClass", (Object) interfaceClass)
                .addPropertyValue("interfaceClassLoader", interfaceClass.getClassLoader());
        return builder.getBeanDefinition();
    }

    protected List<Class<?>> ensureDubboServiceInterfaceClass(Class<?> clazz) {
        Class<?>[] interfaces = ReflectUtil.getInterfaces(clazz);
        if (interfaces.length == 1) {
            return Collections.singletonList(interfaces[0]);
        }
        return Arrays.stream(interfaces)
                .filter(e -> CommonUtil.notEmpty(ReflectUtil.getMethods(clazz, false)))
                .collect(Collectors.toList());
    }

    protected void resolveDubboReference(BeanFactory beanFactory, BeanDefinition beanDefinition) {
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(beanDefinition.getBeanType());
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            DubboReference dubboReference = AnnotationUtil.findAnnotation(entry.getValue(), DubboReference.class);
            if (dubboReference != null) {
                beanFactory.registerBeanDefinition(this.buildDubboReference(entry.getValue().getType(), dubboReference), false);
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static String generateServiceBeanName(DubboService dubboService, Class<?> clazz) {
        StringBuilder builder = new StringBuilder("dubbo:service:" + BeanDefinitionBuilder.resolveBeanName(clazz));
        Mapping.from(dubboService).notNullMap(DubboService::group).whenNotEmpty(g -> builder.append(':').append(g));
        Mapping.from(dubboService).notNullMap(DubboService::version).whenNotEmpty(v -> builder.append(':').append(v));
        return builder.toString();
    }

    @SuppressWarnings("DuplicatedCode")
    public static String generateReferenceBeanName(DubboReference dubboService, Class<?> clazz) {
        StringBuilder builder = new StringBuilder("dubbo:reference:" + BeanDefinitionBuilder.resolveBeanName(clazz));
        Mapping.from(dubboService).notNullMap(DubboReference::group).whenNotEmpty(g -> builder.append(':').append(g));
        Mapping.from(dubboService).notNullMap(DubboReference::version).whenNotEmpty(v -> builder.append(':').append(v));
        return builder.toString();
    }
}
