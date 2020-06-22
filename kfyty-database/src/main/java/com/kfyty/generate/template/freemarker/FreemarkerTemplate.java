package com.kfyty.generate.template.freemarker;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
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
    public void generate(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        try {
            variable.put("basePackage", basePackage);
            loadVariables(dataBaseInfo);
            FreemarkerUtil.loadTemplate(this.prefix, this.template).process(this.variable, out);
        } catch (TemplateException e) {
            log.error("generate source error: ", e);
        }
    }

    private void loadVariables(AbstractDataBaseInfo dataBaseInfo) {
        variable.put("database", dataBaseInfo.getDataBaseName());
        variable.put("table", dataBaseInfo.getTableName());
        variable.put("note", dataBaseInfo.getTableComment());
        variable.put("className", CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true));
        variable.put("classVariable", CommonUtil.convert2Hump(dataBaseInfo.getTableName()));
        List<AbstractTableInfo> fields = new ArrayList<>(dataBaseInfo.getTableInfos().size());
        List<AbstractTableInfo> columns = new ArrayList<>(dataBaseInfo.getTableInfos().size());
        for (AbstractTableInfo tableInfo : dataBaseInfo.getTableInfos()) {
            AbstractTableInfo info = new AbstractTableInfo();
            info.setTableName(tableInfo.getTableName());
            info.setField(CommonUtil.convert2Hump(tableInfo.getField()));
            info.setFieldType(this.convert2JavaType(tableInfo.getFieldType()));
            info.setFieldComment(CommonUtil.empty(tableInfo.getFieldComment()) ? "" : tableInfo.getFieldComment());
            if(tableInfo.primaryKey()) {
                variable.put("pkField", info);
                variable.put("pkColumn", tableInfo);
                continue;
            }
            fields.add(info);
            columns.add(tableInfo);
        }
        variable.put("fields", fields);
        variable.put("columns", columns);
    }
}
