package com.kfyty.excel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述: 表头
 *
 * @author kfyty725
 * @date 2022/6/29 17:21
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTitle {
    /**
     * 表头 key
     */
    private String key;

    /**
     * 表头
     */
    private String title;

    /**
     * 表头排序
     */
    private double order;
}
