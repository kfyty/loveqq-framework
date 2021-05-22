package com.kfyty.database.generate.template.entity;

import com.kfyty.database.generate.GenerateSourcesBufferedWriter;
import com.kfyty.database.generate.info.AbstractTableStructInfo;
import com.kfyty.database.generate.info.AbstractFieldStructInfo;
import com.kfyty.database.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;

import java.io.IOException;

/**
 * 功能描述: 生成 entity 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/13 17:33:27
 * @since JDK 1.8
 */
public class EntityTemplate implements AbstractGenerateTemplate {
    protected String packageName;
    protected String className;
    protected String classVariableName;
    protected String classQualifiedName;

    protected String entityPackageName;
    protected String entityClassName;
    protected String entityClassVariableName;
    protected String entityClassQualifiedName;

    private String initPackageName(String basePackage, String suffix) {
        if(CommonUtil.empty(basePackage) && CommonUtil.empty(suffix)) {
            return "";
        }
        if(!CommonUtil.empty(basePackage) && !CommonUtil.empty(suffix)) {
            return CommonUtil.fillString("{}.{}", basePackage, suffix.toLowerCase().replace("impl", ".impl"));
        }
        return !CommonUtil.empty(basePackage) ? basePackage : suffix.toLowerCase().replace("impl", ".impl");
    }

    protected void initGenerateData(AbstractTableStructInfo tableInfo, String basePackage) {
        this.packageName = initPackageName(basePackage, classSuffix());
        this.className = CommonUtil.convert2Hump(tableInfo.getTableName(), true) + classSuffix();
        this.classVariableName = CommonUtil.convert2Hump(tableInfo.getTableName()) + classSuffix();
        this.classQualifiedName = CommonUtil.empty(packageName) ? className : packageName + "." + className;
        this.entityPackageName = initPackageName(basePackage, entityClassSuffix());
        this.entityClassName = CommonUtil.convert2Hump(tableInfo.getTableName(), true) + entityClassSuffix();
        this.entityClassVariableName = CommonUtil.convert2Hump(tableInfo.getTableName()) + entityClassSuffix();
        this.entityClassQualifiedName = CommonUtil.empty(entityPackageName) ? entityClassName : entityPackageName + "." + entityClassName;
    }

    @Override
    public String classSuffix() {
        return "";
    }

    public String entityClassSuffix() {
        return "";
    }

    @Override
    public void generate(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        initGenerateData(tableInfo, basePackage);
        generatePackage(tableInfo, basePackage, out);
        generateImport(tableInfo, basePackage, out);
        generateClassComment(tableInfo, out);
        generateClassAnnotation(tableInfo, out);
        generateClassDefinition(tableInfo, out);
        out.write(CommonUtil.empty(generateExtendsClass(tableInfo)) ? "" : " extends " + generateExtendsClass(tableInfo));
        out.write(CommonUtil.empty(generateExtendsInterfaces(tableInfo)) ? "" : " extends " + generateExtendsInterfaces(tableInfo));
        out.write(CommonUtil.empty(generateImplementsInterfaces(tableInfo)) ? "" : " implements " + generateImplementsInterfaces(tableInfo));
        out.writeLine(" {");
        generateTableInfo(tableInfo, out);
        generateCustomCode(tableInfo, basePackage, out);
        out.writeLine("}");
    }

    public void generatePackage(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("package {};\n", this.packageName);
    }

    public void importEntity(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("import {};\n", this.entityClassQualifiedName);
    }

    public void generateImport(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("import java.util.Date;\n");
        out.writeLine("import lombok.Data;\n");
    }

    public void generateClassDefinition(AbstractTableStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.write("public class {}", this.className);
    }

    public void generateClassComment(AbstractTableStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("/**");
        out.writeLine(" * TABLE_NAME: {}", tableInfo.getTableName());
        out.writeLine(" * TABLE_COMMENT: {}", tableInfo.getTableComment());
        out.writeLine(" *");
        out.writeLine(" * @author kfyty");
        out.writeLine(" * @email kfyty725@hotmail.com");
        out.writeLine(" */");
    }

    public String generateExtendsClass(AbstractTableStructInfo tableInfo) throws IOException {
        return "";
    }

    public String generateExtendsInterfaces(AbstractTableStructInfo tableInfo) {
        return "";
    }

    public String generateImplementsInterfaces(AbstractTableStructInfo tableInfo) throws IOException {
        return "";
    }

    public void generateClassAnnotation(AbstractTableStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("@Data");
    }

    public void generateTableInfo(AbstractTableStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        for (AbstractFieldStructInfo fieldInfo : tableInfo.getFieldInfos()) {
            generateFieldComment(fieldInfo, out);
            generateFieldAnnotation(fieldInfo, out);
            out.writeLine("\tprivate {} {};\n", convert2JavaType(fieldInfo.getFieldType()), CommonUtil.convert2Hump(fieldInfo.getField(), false));
        }
    }

    public void generateFieldComment(AbstractFieldStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("\t/**");
        out.writeLine("\t * {}", tableInfo.getFieldComment());
        out.writeLine("\t */");
    }

    public void generateFieldAnnotation(AbstractFieldStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {

    }

    public void generateCustomCode(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {

    }
}
