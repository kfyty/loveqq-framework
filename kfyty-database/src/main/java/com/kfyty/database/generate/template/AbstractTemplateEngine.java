package com.kfyty.database.generate.template;

import com.kfyty.database.generate.info.AbstractFieldStructInfo;
import com.kfyty.database.generate.info.AbstractTableStructInfo;
import com.kfyty.database.util.TemplateEngineUtil;
import com.kfyty.util.CommonUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 14:30
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public abstract class AbstractTemplateEngine implements AbstractGenerateTemplate {
    protected String prefix;
    protected String template;
    protected Map<Object, Object> variable;

    public AbstractTemplateEngine(String prefix, String template) {
        try {
            this.prefix = prefix;
            this.template = template;
            this.variable = new HashMap<>(TemplateEngineUtil.loadGenerateProperties());
        } catch (IOException e) {
            log.error("init template error: ", e);
        }
    }

    @Override
    public String classSuffix() {
        String suffix = template.substring(0, template.indexOf("."));
        return suffix.endsWith("_NoSu") ? "" : CommonUtil.convert2Hump(suffix, true);
    }

    @Override
    public String fileTypeSuffix() {
        String regex = "\\." + template.substring(template.lastIndexOf('.') + 1) + "$";
        String s = template.toLowerCase().replaceAll(regex, "");
        return s.substring(s.lastIndexOf("."));
    }

    public abstract List<? extends AbstractGenerateTemplate> loadTemplates(String prefix) throws Exception;

    protected void loadVariables(AbstractTableStructInfo tableInfo, String basePackage) {
        variable.put("basePackage", basePackage);
        variable.put("database", tableInfo.getDataBaseName());
        variable.put("table", tableInfo.getTableName());
        variable.put("note", tableInfo.getTableComment());
        variable.put("className", CommonUtil.convert2Hump(tableInfo.getTableName(), true));
        variable.put("classVariable", CommonUtil.convert2Hump(tableInfo.getTableName()));
        List<AbstractFieldStructInfo> fields = new ArrayList<>(tableInfo.getFieldInfos().size());
        List<AbstractFieldStructInfo> columns = new ArrayList<>(tableInfo.getFieldInfos().size());
        for (AbstractFieldStructInfo fieldInfo : tableInfo.getFieldInfos()) {
            AbstractFieldStructInfo info = new AbstractFieldStructInfo();
            info.setTableName(fieldInfo.getTableName());
            info.setField(CommonUtil.convert2Hump(fieldInfo.getField()));
            info.setFieldType(this.convert2JavaType(fieldInfo.getFieldType()));
            info.setFieldComment(CommonUtil.empty(fieldInfo.getFieldComment()) ? "" : fieldInfo.getFieldComment());
            fieldInfo.setFieldType(CommonUtil.convert2JdbcType(fieldInfo.getFieldType()));
            if(fieldInfo.primaryKey()) {
                variable.put("pkField", info);
                variable.put("pkColumn", fieldInfo);
                continue;
            }
            fields.add(info);
            columns.add(fieldInfo);
        }
        variable.put("fields", fields);
        variable.put("columns", columns);
    }
}
