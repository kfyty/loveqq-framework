package com.kfyty.generate.template.mapper;

import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.template.pojo.GeneratePojoTemplate;
import com.kfyty.util.CommonUtil;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 功能描述: 生成 mapper 接口模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/3 17:37
 * @since JDK 1.8
 */
public class GenerateMapperInterfaceTemplate extends GeneratePojoTemplate {
    @Override
    public String fileSuffix() {
        return "Mapper";
    }

    public String entityFileSuffix() {
        return "Pojo";
    }

    @Override
    public void generate(AbstractDataBaseInfo dataBaseInfo, String basePackage, BufferedWriter out) throws IOException {
        if(!CommonUtil.empty(basePackage)) {
            out.write("package " + basePackage + "." + fileSuffix().toLowerCase() + ";\n\n");
        }
        String className = CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true);
        generateImport(dataBaseInfo, basePackage, out);
        generateClassComment(dataBaseInfo, out);
        generateClassAnnotation(dataBaseInfo, out);
        out.write("public interface " + className + fileSuffix());
        out.write(CommonUtil.empty(generateExtendsInterfaces(dataBaseInfo)) ? "" : " implements " + generateExtendsInterfaces(dataBaseInfo));
        out.write(" {\n\n");

        out.write("\tList<" + className + entityFileSuffix() + "> findBy(@Param(\"field\") String field, @Param(\"value\") Object value) throws SQLException;\n\n");

        generateCustomerMapperInterfaces(dataBaseInfo, basePackage, out);
        out.write("}\n");
    }

    @Override
    public void generateImport(AbstractDataBaseInfo dataBaseInfo, String basePackage, BufferedWriter out) throws IOException {
        String packageName = (CommonUtil.empty(basePackage) ? "" : basePackage + ".") + entityFileSuffix().toLowerCase() + ".";
        out.write("import " + packageName + CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true) + entityFileSuffix() + ";\n\n");
        out.write("import org.apache.ibatis.annotations.Param;\n");
        out.write("import org.springframework.stereotype.Repository;\n\n");
        out.write("import java.sql.SQLException;\n\n");
    }

    @Override
    public void generateClassAnnotation(AbstractDataBaseInfo dataBaseInfo, BufferedWriter out) throws IOException {
        out.write("@Repository\n");
    }

    public String generateExtendsInterfaces(AbstractDataBaseInfo dataBaseInfo) {
        return "";
    }

    public void generateCustomerMapperInterfaces(AbstractDataBaseInfo dataBaseInfo, String basePackage, BufferedWriter out) {

    }
}
