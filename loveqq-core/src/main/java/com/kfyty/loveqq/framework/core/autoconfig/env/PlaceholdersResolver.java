package com.kfyty.loveqq.framework.core.autoconfig.env;

/**
 * 描述: 占位符解析
 *
 * @author kfyty725
 * @date 2022/11/12 17:54
 * @email kfyty725@hotmail.com
 */
public interface PlaceholdersResolver {
    /**
     * 解析表达式中的占位符，占位符取值只能从配置文件中取值
     *
     * @param value 表达式
     * @return 解析后的表达式
     */
    String resolvePlaceholders(String value);
}
