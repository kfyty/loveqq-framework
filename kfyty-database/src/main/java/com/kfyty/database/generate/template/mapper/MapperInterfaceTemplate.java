package com.kfyty.database.generate.template.mapper;

import com.kfyty.database.generate.info.AbstractFieldStructInfo;
import com.kfyty.database.generate.info.AbstractTableStructInfo;
import com.kfyty.database.generate.template.entity.EntityTemplate;
import com.kfyty.support.io.SimpleBufferedWriter;

import java.io.IOException;

/**
 * 功能描述: 生成 mapper 接口模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/3 17:37
 * @since JDK 1.8
 */
public class MapperInterfaceTemplate extends EntityTemplate {
    protected AbstractFieldStructInfo pkInfo;

    @Override
    protected void initGenerateData(AbstractTableStructInfo tableInfo, String basePackage) {
        super.initGenerateData(tableInfo, basePackage);
        this.pkInfo = tableInfo.getFieldInfos().stream().filter(AbstractFieldStructInfo::primaryKey).findFirst().orElse(null);
    }

    @Override
    public String classSuffix() {
        return "Mapper";
    }

    @Override
    public void generateImport(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        super.importEntity(tableInfo, basePackage, out);
        out.writeLine("import org.apache.ibatis.annotations.Param;");
        out.writeLine("import org.springframework.stereotype.Repository;\n");
        out.writeLine("import java.sql.SQLException;\n");
        if(pkInfo == null) {
            out.writeLine("import java.io.Serializable;\n");
        }
    }

    @Override
    public void generateClassDefinition(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.write("public interface {}", this.className);
    }

    @Override
    public void generateClassAnnotation(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.writeLine("@Mapper");
    }

    @Override
    public void generateTableInfo(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {

    }

    @Override
    public void generateCustomCode(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        generateMapperInterfaces(tableInfo, basePackage, out);
    }

    public void generateMapperInterfaces(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        out.writeLine("\n\t{} findById(@Param(\"id\") {} id) throws SQLException;\n", this.entityClassName, pkInfo == null ? "Serializable" : convert2JavaType(pkInfo.getFieldType()));

        out.writeLine("\tList<{}> findBy(@Param(\"field\") String field, @Param(\"value\") Object value) throws SQLException;\n", this.entityClassName);

        out.writeLine("\tList<{}> findLike(@Param(\"field\") String field, @Param(\"value\") Object value) throws SQLException;\n", this.entityClassName);

        out.writeLine("\tList<{}> findAll() throws SQLException;\n", this.entityClassName);

        out.writeLine("\tvoid insert(@Param(\"{}\") {} {}) throws SQLException;\n", this.entityClassVariableName, this.entityClassName, this.entityClassVariableName);

        out.writeLine("\tvoid updateById(@Param(\"id\") {} id, @Param(\"{}\") {} {}) throws SQLException;\n", pkInfo == null ? "Serializable" : convert2JavaType(pkInfo.getFieldType()), this.entityClassVariableName, this.entityClassName, this.entityClassVariableName);

        out.writeLine("\tvoid deleteById(@Param(\"id\") {} id) throws SQLException;\n", pkInfo == null ? "Serializable" : convert2JavaType(pkInfo.getFieldType()));
    }
}
