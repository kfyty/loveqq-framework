package com.kfyty.excel;

import com.kfyty.excel.annotation.TemplateExcel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/7/3 18:20
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportEntity {
    @TemplateExcel("id")
    private Long id;

    @TemplateExcel("姓名")
    private String name;
}
