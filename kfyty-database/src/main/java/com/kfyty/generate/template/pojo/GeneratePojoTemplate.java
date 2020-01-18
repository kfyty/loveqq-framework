package com.kfyty.generate.template.pojo;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;

import java.io.IOException;

/**
 * 功能描述: 生成 pojo 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/13 17:33:27
 * @since JDK 1.8
 */
public class GeneratePojoTemplate implements AbstractGenerateTemplate {
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

    protected void initGenerateData(AbstractDataBaseInfo dataBaseInfo, String basePackage) {
        this.packageName = initPackageName(basePackage, fileSuffix());
        this.className = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true) + fileSuffix();
        this.classVariableName = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), false) + fileSuffix();
        this.classQualifiedName = CommonUtil.empty(packageName) ? className : packageName + "." + className;
        this.entityPackageName = initPackageName(basePackage, entityFileSuffix());
        this.entityClassName = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true) + entityFileSuffix();
        this.entityClassVariableName = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), false) + entityFileSuffix();
        this.entityClassQualifiedName = CommonUtil.empty(entityPackageName) ? entityClassName : entityPackageName + "." + entityClassName;
    }

    @Override
    public String fileSuffix() {
        return "Pojo";
    }

    public String entityFileSuffix() {
        return "Pojo";
    }

    @Override
    public void generate(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        initGenerateData(dataBaseInfo, basePackage);
        generatePackage(dataBaseInfo, basePackage, out);
        generateImport(dataBaseInfo, basePackage, out);
        generateClassComment(dataBaseInfo, out);
        generateClassAnnotation(dataBaseInfo, out);
        generateClassDefinition(dataBaseInfo, out);
        out.write(CommonUtil.empty(generateExtendsClass(dataBaseInfo)) ? "" : " extends " + generateExtendsClass(dataBaseInfo));
        out.write(CommonUtil.empty(generateExtendsInterfaces(dataBaseInfo)) ? "" : " extends " + generateExtendsInterfaces(dataBaseInfo));
        out.write(CommonUtil.empty(generateImplementsInterfaces(dataBaseInfo)) ? "" : " implements " + generateImplementsInterfaces(dataBaseInfo));
        out.writeLine(" {");
        generateTableInfo(dataBaseInfo, out);
        generateCustomCode(dataBaseInfo, basePackage, out);
        out.writeLine("}");
    }

    public void generatePackage(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("package {};\n", this.packageName);
    }

    public void importEntity(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("import {};\n", this.entityClassQualifiedName);
    }

    public void generateImport(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("import java.util.Date;\n");
        out.writeLine("import lombok.Data;\n");
    }

    public void generateClassDefinition(AbstractDataBaseInfo dataBaseInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.write("public class {}", this.className);
    }

    public void generateClassComment(AbstractDataBaseInfo dataBaseInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("/**");
        out.writeLine(" * TABLE_NAME: {}", dataBaseInfo.getTableName());
        out.writeLine(" * TABLE_COMMENT: {}", dataBaseInfo.getTableComment());
        out.writeLine(" *");
        out.writeLine(" * By kfyty");
        out.writeLine(" */");
    }

    public String generateExtendsClass(AbstractDataBaseInfo dataBaseInfo) throws IOException {
        return "";
    }

    public String generateExtendsInterfaces(AbstractDataBaseInfo dataBaseInfo) {
        return "";
    }

    public String generateImplementsInterfaces(AbstractDataBaseInfo dataBaseInfo) throws IOException {
        return "";
    }

    public void generateClassAnnotation(AbstractDataBaseInfo dataBaseInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("@Data");
    }

    public void generateTableInfo(AbstractDataBaseInfo dataBaseInfo, GenerateSourcesBufferedWriter out) throws IOException {
        for (AbstractTableInfo tableInfo : dataBaseInfo.getTableInfos()) {
            generateFieldComment(tableInfo, out);
            generateFieldAnnotation(tableInfo, out);
            out.writeLine("\tprivate {} {};\n", convert2JavaType(tableInfo.getFieldType()), CommonUtil.convert2Hump(tableInfo.getField(), false));
        }
    }

    public void generateFieldComment(AbstractTableInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("\t/**");
        out.writeLine("\t * {}", tableInfo.getFieldComment());
        out.writeLine("\t */");
    }

    public void generateFieldAnnotation(AbstractTableInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {

    }

    public void generateCustomCode(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {

    }
}
