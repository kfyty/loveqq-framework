package com.kfyty.generate.pojo;

import com.kfyty.generate.pojo.info.AbstractDataBaseInfo;
import com.kfyty.generate.pojo.info.AbstractTableInfo;
import com.kfyty.util.CommonUtil;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 功能描述: 生成模板
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/13 17:33:27
 * @since JDK 1.8
 */
public interface GenerateTemplate {

    default String convert2JavaType(String dataBaseType) {
        switch (dataBaseType.toLowerCase()) {
            case "varchar":
                return "String";
            case "varchar2":
                return "String";
            case "nvarchar2" :
                return "String";
            case "number":
                return "Long";
            case "int":
                return "Integer";
            case "integer":
                return "Integer";
            case "bigint":
                return "Long";
            case "float":
                return "Float";
            case "text":
                return "String";
            case "date":
                return "Date";
            case "datetime":
                return "Date";
            case "blob" :
                return "byte[]";
            default :
                throw new IllegalArgumentException("no java data type matched for data base type: [" + dataBaseType + "], please override convert2JavaType method !");
        }
    }

    default String fileSuffix() {
        return "Pojo";
    }

    default String fileTypeSuffix() {
        return ".java";
    }

    default void generate(AbstractDataBaseInfo dataBaseInfo, String packageName, BufferedWriter out) throws IOException {
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

    default void generateImport(AbstractDataBaseInfo dataBaseInfo, BufferedWriter out) throws IOException {
        out.write("import java.util.Date;\n\n");
        out.write("import lombok.Data;\n\n");
    }

    default void generateClassAnnotation(AbstractDataBaseInfo dataBaseInfo, BufferedWriter out) throws IOException {
        out.write("@Data\n");
    }

    default void generateFieldAnnotation(AbstractTableInfo tableInfo, BufferedWriter out) throws IOException {

    }
}
