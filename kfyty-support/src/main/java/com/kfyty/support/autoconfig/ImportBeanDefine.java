package com.kfyty.support.autoconfig;

import com.kfyty.support.autoconfig.beans.BeanDefinition;

import java.util.Set;

/**
 * 描述: 导入自定义的 bean 类型，可以从以扫描的 bean 中过滤得到
 * 实现此接口的 bean 的依赖，如果注入了自己导入的依赖，那么其必须设置为 Lazy
 *
 * @author kfyty725
 * @date 2021/5/21 16:43
 * @email kfyty725@hotmail.com
 */
public interface ImportBeanDefine {
    /**
     * 导入自定义的 bean 类型参与后续实例化
     * @param scanClasses 根据配置扫描到的 Class
     */
    Set<BeanDefinition> doImport(Set<Class<?>> scanClasses);
}
