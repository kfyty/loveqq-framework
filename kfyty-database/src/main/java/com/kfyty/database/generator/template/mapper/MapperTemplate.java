package com.kfyty.database.generator.template.mapper;

import com.kfyty.support.io.SimpleBufferedWriter;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.generator.info.AbstractFieldStructInfo;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.JdbcTypeUtil;
import lombok.Getter;

import java.io.IOException;

/**
 * 功能描述: 生成 mapper 映射文件模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/3 18:19
 * @since JDK 1.8
 */
@Getter
public class MapperTemplate extends MapperInterfaceTemplate {

    @Override
    public String classSuffix() {
        return "Mapper";
    }

    @Override
    public String fileTypeSuffix() {
        return ".xml";
    }

    @Override
    public void doGenerate(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        initTemplateData(tableInfo, basePackage);
        out.writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.writeLine("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0 //EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        out.writeLine("<mapper namespace=\"{}\">\n", this.classQualifiedName);
        doGenerateMapper(tableInfo, basePackage, out);
        out.writeLine("</mapper>");
    }

    public void doGenerateMapper(AbstractTableStructInfo tableInfo, String basePackage, SimpleBufferedWriter out) throws IOException {
        out.writeLine("\t<select id=\"findById\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {} where {} = #{id}", tableInfo.getTableName(), pkInfo == null ? "id" : pkInfo.getField());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findBy\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {} where ${field} = #{value}", tableInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findLike\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {} where ${field} like '%${value}%'", tableInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findAll\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {}", tableInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<insert id=\"insert\" parameterType=\"{}\">", this.entityClassQualifiedName);
        out.write("\t\tinsert into {} (", tableInfo.getTableName());
        for (int i = 0; i < tableInfo.getFieldInfos().size(); i++) {
            out.write(tableInfo.getFieldInfos().get(i).getField());
            if(i != tableInfo.getFieldInfos().size() - 1) {
                out.write(", ");
            }
        }
        out.write(") values (");
        for (int i = 0; i < tableInfo.getFieldInfos().size(); i++) {
            String field = tableInfo.getFieldInfos().get(i).getField();
            out.write("#{{}.{}}", this.entityClassVariableName, CommonUtil.underline2CamelCase(field, false));
            if(i != tableInfo.getFieldInfos().size() - 1) {
                out.write(", ");
            }
        }
        out.writeLine(")");
        out.writeLine("\t</insert>\n");

        out.writeLine("\t<update id=\"updateById\">");
        out.writeLine("\t\tupdate {}", tableInfo.getTableName());
        out.writeLine("\t\t<set>");
        for (AbstractFieldStructInfo fieldInfo : tableInfo.getFieldInfos()) {
            String field = fieldInfo.getField();
            String classField = CommonUtil.underline2CamelCase(field, false);
            if(fieldInfo.primaryKey()) {
                continue;
            }
            if(!"String".equals(JdbcTypeUtil.convert2JavaType(fieldInfo.getFieldType()))) {
                out.writeLine("\t\t\t<if test=\"{}.{} != null\">", this.entityClassVariableName, classField);
            } else {
                out.writeLine("\t\t\t<if test=\"{}.{} != null and {}.{} != ''\">", this.entityClassVariableName, classField, this.entityClassVariableName, classField);
            }
            out.writeLine("\t\t\t\t{} = #{{}.{}}", field, this.entityClassVariableName, classField);
            out.writeLine("\t\t\t</if>");
        }
        out.writeLine("\t\t</set>");
        out.writeLine("\t\twhere {} = #{id}", pkInfo == null ? "id" : pkInfo.getField());
        out.writeLine("\t</update>\n");

        out.writeLine("\t<delete id=\"deleteById\">");
        out.writeLine("\t\tdelete from {} where {} = #{id}", tableInfo.getTableName(), pkInfo == null ? "id" : pkInfo.getField());
        out.writeLine("\t</delete>\n");
    }
}
