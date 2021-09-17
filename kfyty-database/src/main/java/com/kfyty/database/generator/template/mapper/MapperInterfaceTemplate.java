package com.kfyty.database.generator.template.mapper;

import com.kfyty.database.generator.info.AbstractFieldStructInfo;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.generator.template.entity.EntityTemplate;
import com.kfyty.support.io.SimpleBufferedWriter;
import lombok.Getter;

import java.io.IOException;

/**
 * 功能描述: 生成 mapper 接口模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/3 17:37
 * @since JDK 1.8
 */
@Getter
public class MapperInterfaceTemplate extends EntityTemplate {
    protected AbstractFieldStructInfo pkInfo;

    @Override
    public void initTemplateData(AbstractTableStructInfo tableInfo, String basePackage) {
        super.initTemplateData(tableInfo, basePackage);
        this.pkInfo = tableInfo.getFieldInfos().stream().filter(AbstractFieldStructInfo::isPrimaryKey).findFirst().orElse(null);
    }

    @Override
    public String classSuffix() {
        return "Mapper";
    }

    @Override
    public void doGenerateImport(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        super.importEntity(tableInfo, basePackage, out);
        out.writeLine("import org.apache.ibatis.annotations.Param;");
        out.writeLine("import org.springframework.stereotype.Repository;\n");
        out.writeLine("import java.sql.SQLException;\n");
        if(pkInfo == null) {
            out.writeLine("import java.io.Serializable;\n");
        }
    }

    @Override
    public void doGenerateClassDefinition(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.write("public interface {}", this.className);
    }

    @Override
    public void doGenerateClassAnnotation(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {
        out.writeLine("@Mapper");
    }

    @Override
    public void doGenerateTableInfo(AbstractTableStructInfo tableInfo, SimpleBufferedWriter out) throws IOException {

    }

    @Override
    public void doGenerateCustomCode(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
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
