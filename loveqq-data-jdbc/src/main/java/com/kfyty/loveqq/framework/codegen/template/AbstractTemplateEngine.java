package com.kfyty.loveqq.framework.codegen.template;

import com.kfyty.loveqq.framework.codegen.config.GeneratorConfiguration;
import com.kfyty.loveqq.framework.codegen.info.AbstractFieldStructInfo;
import com.kfyty.loveqq.framework.codegen.info.AbstractTableStructInfo;
import com.kfyty.loveqq.framework.codegen.utils.TemplateEngineUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.removePrefix;

/**
 * 描述:
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 14:30
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public abstract class AbstractTemplateEngine implements GeneratorTemplate {
    public static final String PACKAGE_CONFIG = ".package";
    public static final String FILE_PATH_CONFIG = ".path";

    protected String prefix;
    protected String template;
    protected Map<Object, Object> variable;

    public AbstractTemplateEngine(String prefix, String template) {
        this.prefix = prefix;
        this.template = template;
        this.variable = new HashMap<>(TemplateEngineUtil.loadGeneratorProperties());
    }

    @Override
    public String classSuffix() {
        String suffix = template.substring(0, template.indexOf("."));
        return suffix.endsWith("_NoSu") ? "" : suffix;
    }

    @Override
    public String fileTypeSuffix() {
        String regex = "\\." + template.substring(template.lastIndexOf('.') + 1) + "$";
        String s = template.toLowerCase().replaceAll(regex, "");
        return s.substring(s.lastIndexOf("."));
    }

    @Override
    public String packageName() {
        Object o = this.variable.get(this.template + PACKAGE_CONFIG);
        return o == null ? null : o.toString();
    }

    @Override
    public String filePath() {
        Object o = this.variable.get(this.template + FILE_PATH_CONFIG);
        return o == null ? null : o.toString();
    }

    public abstract List<? extends GeneratorTemplate> loadTemplates(String prefix);

    protected String getTableClass(AbstractTableStructInfo tableInfo, GeneratorConfiguration configuration) {
        return !configuration.isRemoveTablePrefix() ? tableInfo.getTableName() : removePrefix(configuration.getTablePrefix().toLowerCase(), tableInfo.getTableName().toLowerCase());
    }

    protected void loadVariables(AbstractTableStructInfo tableInfo, GeneratorConfiguration configuration) {
        variable.put("basePackage", configuration.getBasePackage());
        variable.put("database", tableInfo.getDatabaseName());
        variable.put("table", tableInfo.getTableName());
        variable.put("note", tableInfo.getTableComment());
        variable.put("className", CommonUtil.underline2CamelCase(this.getTableClass(tableInfo, configuration), true));
        variable.put("classVariable", CommonUtil.underline2CamelCase(this.getTableClass(tableInfo, configuration)));
        List<AbstractFieldStructInfo> fields = new ArrayList<>(tableInfo.getFieldInfos().size());
        List<AbstractFieldStructInfo> columns = new ArrayList<>(tableInfo.getFieldInfos().size());
        for (AbstractFieldStructInfo fieldInfo : tableInfo.getFieldInfos()) {
            AbstractFieldStructInfo info = fieldInfo.clone();
            info.setField(CommonUtil.underline2CamelCase(fieldInfo.getField()));
            info.setFieldType(this.convert2JavaType(fieldInfo.getFieldType()));
            info.setFieldComment(CommonUtil.empty(fieldInfo.getFieldComment()) ? "" : fieldInfo.getFieldComment());
            if(fieldInfo.isPrimaryKey()) {
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
