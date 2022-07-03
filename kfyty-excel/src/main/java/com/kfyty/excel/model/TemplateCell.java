package com.kfyty.excel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述: 一个单元格
 *
 * @author kfyty725
 * @date 2022/6/29 17:21
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCell {
    private Object data;
}
