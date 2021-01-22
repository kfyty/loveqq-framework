package com.kfyty.generate.template.mapper;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractTableStructInfo;
import com.kfyty.generate.template.entity.EntityTemplate;

import java.io.IOException;

/**
 * 功能描述: 生成 mapper 接口模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/3 17:37
 * @since JDK 1.8
 */
public class MapperInterfaceTemplate extends EntityTemplate {

    @Override
    public String classSuffix() {
        return "Mapper";
    }

    @Override
    public void generateImport(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        super.importEntity(tableInfo, basePackage, out);
        out.writeLine("import org.apache.ibatis.annotations.Param;");
        out.writeLine("import org.springframework.stereotype.Repository;\n");
        out.writeLine("import java.sql.SQLException;\n");
    }

    @Override
    public void generateClassDefinition(AbstractTableStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.write("public interface {}", this.className);
    }

    @Override
    public void generateClassAnnotation(AbstractTableStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("@Mapper");
    }

    @Override
    public void generateTableInfo(AbstractTableStructInfo tableInfo, GenerateSourcesBufferedWriter out) throws IOException {

    }

    @Override
    public void generateCustomCode(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        generateMapperInterfaces(tableInfo, basePackage, out);
    }

    public void generateMapperInterfaces(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("\n\t{} findById(@Param(\"id\") String id) throws SQLException;\n", this.entityClassName);

        out.writeLine("\tList<{}> findBy(@Param(\"field\") String field, @Param(\"value\") Object value) throws SQLException;\n", this.entityClassName);

        out.writeLine("\tList<{}> findLike(@Param(\"field\") String field, @Param(\"value\") Object value) throws SQLException;\n", this.entityClassName);

        out.writeLine("\tList<{}> findAll() throws SQLException;\n", this.entityClassName);

        out.writeLine("\tvoid insert(@Param(\"{}\") {} {}) throws SQLException;\n", this.entityClassVariableName, this.entityClassName, this.entityClassVariableName);

        out.writeLine("\tvoid updateById(@Param(\"id\") String id, @Param(\"{}\") {} {}) throws SQLException;\n", this.entityClassVariableName, this.entityClassName, this.entityClassVariableName);

        out.writeLine("\tvoid deleteById(@Param(\"id\") String id) throws SQLException;\n");
    }
}
