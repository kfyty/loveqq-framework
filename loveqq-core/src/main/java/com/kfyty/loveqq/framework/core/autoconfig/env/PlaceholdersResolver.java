package com.kfyty.loveqq.framework.core.autoconfig.env;

/**
 * 描述: 占位符解析
 *
 * @author kfyty725
 * @date 2022/11/12 17:54
 * @email kfyty725@hotmail.com
 */
public interface PlaceholdersResolver {
    String resolvePlaceholders(String value);
}
