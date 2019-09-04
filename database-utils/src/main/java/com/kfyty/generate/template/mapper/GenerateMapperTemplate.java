package com.kfyty.generate.template.mapper;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
import com.kfyty.generate.template.pojo.GeneratePojoTemplate;
import com.kfyty.util.CommonUtil;

import java.io.IOException;

/**
 * 功能描述: 生成 mapper 映射文件模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/3 18:19
 * @since JDK 1.8
 */
public class GenerateMapperTemplate extends GeneratePojoTemplate {

    public String mapperFileSuffix() {
        return "Mapper";
    }

    @Override
    public String fileSuffix() {
        return "Mapper";
    }

    @Override
    public String fileTypeSuffix() {
        return ".xml";
    }

    @Override
    public void generate(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        String mapperPackageName = (CommonUtil.empty(basePackage) ? "" : basePackage + ".") + mapperFileSuffix().toLowerCase() + ".";
        String mapperClassName = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true) + mapperFileSuffix();
        out.writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.writeLine("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0 //EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        out.writeLine("<mapper namespace=\"{}\">\n", mapperPackageName +mapperClassName);
        generateMapper(dataBaseInfo, basePackage, out);
        out.writeLine("</mapper>");
    }

    public void generateMapper(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        String entityPackageName = (CommonUtil.empty(basePackage) ? "" : basePackage + ".") + entityFileSuffix().toLowerCase() + ".";
        String varName = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), false);
        String className = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true);
        String entityVarName = varName + entityFileSuffix();
        String entityClassName = className + entityFileSuffix();

        out.writeLine("\t<select id=\"findById\" resultType=\"{}\">", entityPackageName + entityClassName);
        out.writeLine("\t\tselect * from {} where id = #{id}", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findBy\" resultType=\"{}\">", entityPackageName + entityClassName);
        out.writeLine("\t\tselect * from {} where ${field} = #{value}", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findLike\" resultType=\"{}\">", entityPackageName + entityClassName);
        out.writeLine("\t\tselect * from {} where ${field} like '%${value}%'", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findAll\" resultType=\"{}\">", entityPackageName + entityClassName);
        out.writeLine("\t\tselect * from {}", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<insert id=\"insertById\" parameterType=\"{}\">", entityPackageName + entityClassName);
        out.write("\t\tinsert into {} values (", dataBaseInfo.getTableName());
        for (int i = 0; i < dataBaseInfo.getTableInfos().size(); i++) {
            String field = dataBaseInfo.getTableInfos().get(i).getField();
            out.write("#{{}.{}}", entityVarName, CommonUtil.convert2Hump(field, false));
            if(i != dataBaseInfo.getTableInfos().size() - 1) {
                out.write(", ");
            }
        }
        out.writeLine(")");
        out.writeLine("\t</insert>\n");

        out.writeLine("\t<update id=\"updateById\">");
        out.writeLine("\t\tupdate {}", dataBaseInfo.getTableName());
        out.writeLine("\t\t<set>");
        for (AbstractTableInfo tableInfo : dataBaseInfo.getTableInfos()) {
            String field = tableInfo.getField();
            String classField = CommonUtil.convert2Hump(field, false);
            if(field.equals("id")) {
                continue;
            }
            if(!CommonUtil.convert2JavaType(tableInfo.getFieldType()).equals("String")) {
                out.writeLine("\t\t\t<if test=\"{}.{} != null\">", entityVarName, classField);
            } else {
                out.writeLine("\t\t\t<if test=\"{}.{} != null and {}.{} != ''\">", entityVarName, classField, entityVarName, classField);
            }
            out.writeLine("\t\t\t\t{} = #{{}.{}}", field, entityVarName, classField);
            out.writeLine("\t\t\t</if>");
        }
        out.writeLine("\t\t</set>");
        out.writeLine("\t\twhere id = #{id}");
        out.writeLine("\t</update>\n");

        out.writeLine("\t<delete id=\"deleteById\">");
        out.writeLine("\t\tdelete from {} where id = #{id}", dataBaseInfo.getTableName());
        out.writeLine("\t</delete>\n");
    }
}
