package com.kfyty.loveqq.framework.data.korm.mapping;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 描述: 动态 SQL 模板
 *
 * @author kfyty725
 * @date 2021/9/28 13:06
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract class TemplateStatement {
    private String id;
    private String labelType;

    public TemplateStatement(String id, String labelType) {
        this.id = id;
        this.labelType = labelType;
    }
}
