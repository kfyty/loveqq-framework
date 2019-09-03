package com.kfyty.generate.template.mapper;

import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 功能描述: 生成 mapper 映射文件模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/3 18:19
 * @since JDK 1.8
 */
public class GenerateMapperTemplate implements AbstractGenerateTemplate {
    @Override
    public String fileSuffix() {
        return "Mapper";
    }

    @Override
    public String fileTypeSuffix() {
        return ".xml";
    }

    public String mapperFileSuffix() {
        return "Mapper";
    }

    public String entityFileSuffix() {
        return "Pojo";
    }

    @Override
    public void generate(AbstractDataBaseInfo dataBaseInfo, String basePackage, BufferedWriter out) throws IOException {
        String packageName = (CommonUtil.empty(basePackage) ? "" : basePackage + ".") + mapperFileSuffix().toLowerCase() + ".";
        String className = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true);
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0 //EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
        out.write("<mapper namespace=\"" + packageName + className + mapperFileSuffix() + "\">\n");

        out.write("\t<select id=\"findBy\" resultType=\"" + packageName + className + entityFileSuffix() + "\">\n");
        out.write("\t\tselect * from " + dataBaseInfo.getTableName() + " where ${field} = #{value}\n");
        out.write("\t</select>\n\n");

        generateCustomerMapper(dataBaseInfo, basePackage, out);
        out.write("</mapper>\n");
    }

    public void generateCustomerMapper(AbstractDataBaseInfo dataBaseInfo, String basePackage, BufferedWriter out) {

    }
}
