package com.kfyty.loveqq.framework.data.korm.sql.dynamic.enjoy;

import com.jfinal.template.Engine;
import com.kfyty.database.jdbc.mapping.enjoy.EnjoyTemplateStatement;
import com.kfyty.loveqq.framework.data.korm.sql.dynamic.AbstractDynamicProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.BLANK_LINE_PATTERN;

/**
 * 描述: 基于 enjoy 的动态 SQL 提供者
 *
 * @author kfyty725
 * @date 2021/9/29 22:43
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class EnjoyDynamicProvider extends AbstractDynamicProvider<EnjoyTemplateStatement> {
    protected Engine engine;

    @Override
    public String processTemplate(EnjoyTemplateStatement template, Map<String, Object> params) {
        String sql = template.getTemplate().renderToString(params);
        return sql.replaceAll(BLANK_LINE_PATTERN.pattern(), "").trim();
    }

    @Override
    protected String getTemplateSuffix() {
        return ".xml";
    }

    @Override
    protected EnjoyTemplateStatement buildTemplateStatement(String id, String labelType, String content) {
        return new EnjoyTemplateStatement(id, labelType, this.engine.getTemplateByString(content));
    }
}
