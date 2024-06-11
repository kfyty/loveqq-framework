package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述: 一对值
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@NestedConfigurationProperty
public class Pair<K, V> {
    /**
     * key
     */
    private K key;

    /**
     * value
     */
    private V value;
}
