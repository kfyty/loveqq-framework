package com.kfyty.generate.template;

import com.kfyty.generate.info.AbstractDataBaseInfo;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * 功能描述: 生成模板接口
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/22 16:07:16
 * @since JDK 1.8
 */
public interface AbstractGenerateTemplate {
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
            case "timestamp" :
                return "Date";
            case "blob" :
                return "byte[]";
            default :
                throw new IllegalArgumentException("no java data type matched for data base type: [" + dataBaseType + "], please override convert2JavaType method !");
        }
    }

    default String fileSuffix() {
        return "";
    }

    default String fileTypeSuffix() {
        return ".java";
    }

    default boolean sameFile() {
        return false;
    }

    default void generate(AbstractDataBaseInfo dataBaseInfo, String basePackage, BufferedWriter out) throws IOException {

    }
}
