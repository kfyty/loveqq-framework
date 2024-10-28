package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig;

import com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.annotation.MapperScan;
import com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.support.ConcurrentSqlSession;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.PackageUtil;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述: mapper 接口扫描器
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 * @see MapperAnnotationScanner
 */
public class MapperScanner implements BeanFactoryPostProcessor, ConfigurableApplicationContextAware {
    /**
     * 可配置的上下文
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     * 路径匹配器
     */
    @Autowired
    private PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

    @Override
    public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) {
        // 只取启动类注解，减少搜索次数
        MapperScan mapperScan = AnnotationUtil.findAnnotation(this.applicationContext.getPrimarySource(), MapperScan.class);
        if (mapperScan != null) {
            this.loadMapperScan(beanFactory, mapperScan);
        }
    }

    protected void loadMapperScan(BeanFactory beanFactory, MapperScan mapperScan) {
        Set<Class<?>> classes = new HashSet<>();

        if (CommonUtil.notEmpty(mapperScan.value())) {
            List<Class<?>> collect = Arrays.stream(mapperScan.value()).flatMap(e -> PackageUtil.scanClass(e, this.pathMatchingResourcePatternResolver).stream()).collect(Collectors.toList());
            classes.addAll(collect);
        }

        if (CommonUtil.notEmpty(mapperScan.basePackageClasses())) {
            List<Class<?>> collect = Arrays.stream(mapperScan.basePackageClasses()).flatMap(e -> PackageUtil.scanClass(e).stream()).collect(Collectors.toList());
            classes.addAll(collect);
        }

        if (classes.isEmpty()) {
            return;
        }

        for (Class<?> clazz : classes) {
            if (!clazz.isInterface() || AnnotationUtil.hasAnnotation(clazz, Mapper.class)) {                            // 由 MapperAnnotationScanner 注册
                continue;
            }
            BeanDefinition beanDefinition = genericBeanDefinition(mapperScan.factoryBean())
                    .addConstructorArgs(Class.class, clazz)
                    .addConstructorArgs(Lazy.class, new Lazy<>(() -> this.obtainSqlSession(mapperScan)))
                    .getBeanDefinition();
            beanFactory.registerBeanDefinition(beanDefinition);
        }
    }

    protected ConcurrentSqlSession obtainSqlSession(MapperScan mapperScan) {
        if (CommonUtil.notEmpty(mapperScan.concurrentSqlSession())) {
            return this.applicationContext.getBean(mapperScan.concurrentSqlSession());
        }
        return this.applicationContext.getBean(ConcurrentSqlSession.class);
    }
}
