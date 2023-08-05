package com.kfyty.database.jdbc.mapping.enjoy;

import com.jfinal.template.Template;
import com.kfyty.database.jdbc.mapping.TemplateStatement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 描述: freemarker 动态 SQL 模板
 *
 * @author kfyty725
 * @date 2021/9/29 23:00
 * @email kfyty725@hotmail.com
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EnjoyTemplateStatement extends TemplateStatement {
    private Template template;

    public EnjoyTemplateStatement(String id, String labelType) {
        super(id, labelType);
    }

    public EnjoyTemplateStatement(String id, String labelType, Template template) {
        this(id, labelType);
        this.template = template;
    }
}
