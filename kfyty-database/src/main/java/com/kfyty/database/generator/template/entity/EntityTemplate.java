package com.kfyty.database.generator.template.entity;

import com.kfyty.database.generator.info.AbstractFieldStructInfo;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.support.io.SimpleBufferedWriter;
import com.kfyty.support.utils.CommonUtil;
import lombok.Getter;

import java.io.IOException;

/**
 * 功能描述: 生成 entity 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/13 17:33:27
 * @since JDK 1.8
 */
@Getter
public class EntityTemplate implements GeneratorTemplate {
    protected String packageName;
    protected String className;
    protected String classVariableName;
    protected String classQualifiedName;

    protected String entityPackageName;
    protected String entityClassName;
    protected String entityClassVariableName;
    protected String entityClassQualifiedName;

    protected String initPackageName(String basePackage, String suffix, String packageName) {
        if(CommonUtil.notEmpty(packageName)) {
            return packageName;
        }
        if(CommonUtil.empty(basePackage) && CommonUtil.empty(suffix)) {
            return "";
        }
        if(CommonUtil.notEmpty(basePackage) && CommonUtil.notEmpty(suffix)) {
            return CommonUtil.format("{}.{}", basePackage, suffix.toLowerCase().replace("impl", ".impl"));
        }
        return CommonUtil.notEmpty(basePackage) ? basePackage : suffix.toLowerCase().replace("impl", ".impl");
    }

    public void initTemplateData(AbstractTableStructInfo tableInfo, String basePackage) {
        this.packageName = initPackageName(basePackage, classSuffix(), packageName());
        this.className = CommonUtil.underline2CamelCase(tableInfo.getTableName(), true) + classSuffix();
        this.classVariableName = CommonUtil.underline2CamelCase(tableInfo.getTableName()) + classSuffix();
        this.classQualifiedName = CommonUtil.empty(packageName) ? className : packageName + "." + className;
        this.entityPackageName = initPackageName(basePackage, entityClassSuffix(), entityPackageName());
        this.entityClassName = CommonUtil.underline2CamelCase(tableInfo.getTableName(), true) + entityClassSuffix();
        this.entityClassVariableName = CommonUtil.underline2CamelCase(tableInfo.getTableName()) + entityClassSuffix();
        this.entityClassQualifiedName = CommonUtil.empty(entityPackageName) ? entityClassName : entityPackageName + "." + entityClassName;
    }

    @Override
    public String classSuffix() {
        return "";
    }

    public String entityClassSuffix() {
        return "";
    }

    public String entityPackageName() {
        return "";
    }

    @Override
    public void doGenerate(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        initTemplateData(tableInfo, basePackage);
        doGeneratePackage(tableInfo, basePackage, out);
        doGenerateImport(tableInfo, basePackage, out);
        doGenerateClassComment(tableInfo, out);
        doGenerateClassAnnotation(tableInfo, out);
        doGenerateClassDefinition(tableInfo, out);
        out.write(CommonUtil.empty(doGenerateExtendsClass(tableInfo)) ? "" : " extends " + doGenerateExtendsClass(tableInfo));
        out.write(CommonUtil.empty(doGenerateExtendsInterfaces(tableInfo)) ? "" : " extends " + doGenerateExtendsInterfaces(tableInfo));
        out.write(CommonUtil.empty(doGenerateImplementsInterfaces(tableInfo)) ? "" : " implements " + doGenerateImplementsInterfaces(tableInfo));
        out.writeLine(" {");
        doGenerateTableInfo(tableInfo, out);
        doGenerateCustomCode(tableInfo, basePackage, out);
        out.writeLine("}");
    }

    public void doGeneratePackage(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        out.writeLine("package {};\n", this.packageName);
    }

    public void importEntity(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        out.writeLine("import {};\n", this.entityClassQualifiedName);
    }

    public void doGenerateImport(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        out.writeLine("import java.util.Date;\n");
        out.writeLine("import lombok.Data;\n");
    }

    public void doGenerateClassDefinition(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.write("public class {}", this.className);
    }

    public void doGenerateClassComment(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.writeLine("/**");
        out.writeLine(" * TABLE_NAME: {}", tableInfo.getTableName());
        out.writeLine(" * TABLE_COMMENT: {}", tableInfo.getTableComment());
        out.writeLine(" *");
        out.writeLine(" * @author kfyty");
        out.writeLine(" * @email kfyty725@hotmail.com");
        out.writeLine(" */");
    }

    public String doGenerateExtendsClass(AbstractTableStructInfo tableInfo) throws IOException {
        return "";
    }

    public String doGenerateExtendsInterfaces(AbstractTableStructInfo tableInfo) {
        return "";
    }

    public String doGenerateImplementsInterfaces(AbstractTableStructInfo tableInfo) throws IOException {
        return "";
    }

    public void doGenerateClassAnnotation(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.writeLine("@Data");
    }

    public void doGenerateTableInfo(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        for (AbstractFieldStructInfo fieldInfo : tableInfo.getFieldInfos()) {
            doGenerateFieldComment(fieldInfo, out);
            doGenerateFieldAnnotation(fieldInfo, out);
            out.writeLine("\tprivate {} {};\n", convert2JavaType(fieldInfo.getFieldType()), CommonUtil.underline2CamelCase(fieldInfo.getField(), false));
        }
    }

    public void doGenerateFieldComment(AbstractFieldStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.writeLine("\t/**");
        out.writeLine("\t * {}", tableInfo.getFieldComment());
        out.writeLine("\t */");
    }

    public void doGenerateFieldAnnotation(AbstractFieldStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {

    }

    public void doGenerateCustomCode(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {

    }
}
