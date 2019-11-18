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
    protected String mapperPackageName;
    protected String mapperClassName;
    protected String mapperClassVariableName;
    protected String mapperClassQualifiedName;

    @Override
    protected void initGenerateData(AbstractDataBaseInfo dataBaseInfo, String basePackage) {
        super.initGenerateData(dataBaseInfo, basePackage);
        this.mapperPackageName = (CommonUtil.empty(basePackage) ? "" : basePackage + ".") + mapperFileSuffix().toLowerCase();
        this.mapperClassName = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true) + mapperFileSuffix();
        this.mapperClassVariableName = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), false) + mapperFileSuffix();
        this.mapperClassQualifiedName = this.mapperPackageName + "." + this.mapperClassName;
    }

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
        initGenerateData(dataBaseInfo, basePackage);
        out.writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.writeLine("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0 //EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        out.writeLine("<mapper namespace=\"{}\">\n", this.mapperClassQualifiedName);
        generateMapper(dataBaseInfo, basePackage, out);
        out.writeLine("</mapper>");
    }

    public void generateMapper(AbstractDataBaseInfo dataBaseInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        out.writeLine("\t<select id=\"findById\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {} where id = #{id}", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findBy\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {} where ${field} = #{value}", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findLike\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {} where ${field} like '%${value}%'", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<select id=\"findAll\" resultType=\"{}\">", this.entityClassQualifiedName);
        out.writeLine("\t\tselect * from {}", dataBaseInfo.getTableName());
        out.writeLine("\t</select>\n");

        out.writeLine("\t<insert id=\"insertById\" parameterType=\"{}\">", this.entityClassQualifiedName);
        out.write("\t\tinsert into {} (", dataBaseInfo.getTableName());
        for (int i = 0; i < dataBaseInfo.getTableInfos().size(); i++) {
            out.write(dataBaseInfo.getTableInfos().get(i).getField());
            if(i != dataBaseInfo.getTableInfos().size() - 1) {
                out.write(", ");
            }
        }
        out.write(") values (");
        for (int i = 0; i < dataBaseInfo.getTableInfos().size(); i++) {
            String field = dataBaseInfo.getTableInfos().get(i).getField();
            out.write("#{{}.{}}", this.entityClassVariableName, CommonUtil.convert2Hump(field, false));
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
                out.writeLine("\t\t\t<if test=\"{}.{} != null\">", this.entityClassVariableName, classField);
            } else {
                out.writeLine("\t\t\t<if test=\"{}.{} != null and {}.{} != ''\">", this.entityClassVariableName, classField, this.entityClassVariableName, classField);
            }
            out.writeLine("\t\t\t\t{} = #{{}.{}}", field, this.entityClassVariableName, classField);
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
