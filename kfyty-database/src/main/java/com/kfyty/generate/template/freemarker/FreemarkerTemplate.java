package com.kfyty.generate.template.freemarker;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractTableStructInfo;
import com.kfyty.generate.info.AbstractFieldStructInfo;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;
import com.kfyty.util.FreemarkerUtil;
import freemarker.template.TemplateException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: freemarker 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 14:30
 * @since JDK 1.8
 */
@Slf4j
@Getter
public class FreemarkerTemplate implements AbstractGenerateTemplate {
    private String prefix;
    private String template;
    private Map<Object, Object> variable;

    public FreemarkerTemplate(String prefix, String template) {
        try {
            this.prefix = prefix;
            this.template = template;
            this.variable = new HashMap<>(FreemarkerUtil.loadGenerateProperties());
        } catch (IOException e) {
            log.error("init freemarker template error: ", e);
        }
    }

    @Override
    public void generate(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        try {
            variable.put("basePackage", basePackage);
            loadVariables(tableInfo);
            FreemarkerUtil.loadTemplate(this.prefix, this.template).process(this.variable, out);
        } catch (TemplateException e) {
            log.error("generate source error: ", e);
        }
    }

    private void loadVariables(AbstractTableStructInfo tableInfo) {
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
