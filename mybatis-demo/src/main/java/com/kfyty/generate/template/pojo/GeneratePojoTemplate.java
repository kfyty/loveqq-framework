package com.kfyty.generate.template.pojo;

import com.kfyty.generate.info.AbstractDataBaseInfo;
import com.kfyty.generate.info.AbstractTableInfo;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 功能描述: 生成 pojo 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/13 17:33:27
 * @since JDK 1.8
 */
public class GeneratePojoTemplate implements AbstractGenerateTemplate {
    @Override
    public String fileSuffix() {
        return "Pojo";
    }

    @Override
    public void generate(AbstractDataBaseInfo dataBaseInfo, String packageName, BufferedWriter out) throws IOException {
        if(!CommonUtil.empty(packageName)) {
            out.write("package " + packageName + ";\n\n");
        }
        generateImport(dataBaseInfo, out);
        out.write("/**\n");
        out.write(" * TABLE_NAME: " + dataBaseInfo.getTableName() + "\n");
        out.write(" * TABLE_COMMENT: " + dataBaseInfo.getTableComment() + "\n");
        out.write(" *\n");
        out.write(" * By kfyty\n");
        out.write(" */\n");
        generateClassAnnotation(dataBaseInfo, out);
        out.write("public class " + CommonUtil.convert2Hump(dataBaseInfo.getTableName(), true) + fileSuffix() + " {\n");
        for (AbstractTableInfo tableInfo : dataBaseInfo.getTableInfos()) {
            out.write("\t/**\n");
            out.write("\t * " + tableInfo.getFieldComment() + "\n");
            out.write("\t */\n");
            generateFieldAnnotation(tableInfo, out);
            out.write("\tprivate " + convert2JavaType(tableInfo.getFieldType()) + " " + CommonUtil.convert2Hump(tableInfo.getField(), false) + ";\n\n");
        }
        out.write("}\n");
    }

    public void generateImport(AbstractDataBaseInfo dataBaseInfo, BufferedWriter out) throws IOException {
        out.write("import java.util.Date;\n\n");
        out.write("import lombok.Data;\n\n");
    }

    public void generateClassAnnotation(AbstractDataBaseInfo dataBaseInfo, BufferedWriter out) throws IOException {
        out.write("@Data\n");
    }

    public void generateFieldAnnotation(AbstractTableInfo tableInfo, BufferedWriter out) throws IOException {

    }
}
