package com.kfyty.excel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 描述: 一行数据
 *
 * @author kfyty725
 * @date 2022/6/29 17:21
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRow {
    private List<TemplateCell> cells;
}
