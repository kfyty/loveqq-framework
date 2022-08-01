package com.kfyty.support.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 描述: jdbcType 工具
 *
 * @author kfyty725
 * @date 2021/6/3 9:58
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class JdbcTypeUtil {

    public static String convert2JdbcType(String databaseType) {
        if (databaseType.toLowerCase().contains("timestamp")) {
            return "TIMESTAMP";
        }
        switch (databaseType.toLowerCase()) {
            case "bit":
                return "BIT";
            case "smallint":
                return "SMALLINT";
            case "char":
            case "text":
            case "tinytext":
            case "varchar":
            case "varchar2":
            case "nvarchar2":
                return "VARCHAR";
            case "clob":
                return "CLOB";
            case "nclob":
                return "NCLOB";
            case "longtext":
            case "mediumtext":
            case "long varchar":
                return "LONGVARCHAR";
            case "decimal":
                return "DECIMAL";
            case "bigint":
                return "BIGINT";
            case "long":
            case "number":
            case "numeric":
                return "NUMERIC";
            case "tinyint":
                return "TINYINT";
            case "int":
            case "integer":
                return "INTEGER";
            case "float":
                return "FLOAT";
            case "double":
                return "DOUBLE";
            case "time":
            case "date":
                return "DATE";
            case "datetime":
            case "datetime2":
                return "TIMESTAMP";
            case "blob":
                return "BLOB";
            case "longblob":
            case "binary":
            case "varbinary":
                return "BINARY";
            default:
                log.warn("No jdbc type matched and instead of 'OTHER' !");
                return "OTHER";
        }
    }

    public static String convert2JavaType(String databaseType) {
        if (databaseType.toLowerCase().contains("timestamp")) {
            return "LocalDateTime";
        }
        switch (databaseType.toLowerCase()) {
            case "bit":
                return "Byte";
            case "char":
            case "text":
            case "json":
            case "other":
            case "tinytext":
            case "mediumtext":
            case "longtext":
            case "clob":
            case "nclob":
            case "varchar":
            case "varchar2":
            case "nvarchar2":
                return "String";
            case "decimal":
                return "BigDecimal";
            case "long":
            case "bigint":
            case "number":
            case "numeric":
                return "Long";
            case "tinyint":
            case "smallint":
                return "Short";
            case "int":
            case "integer":
                return "Integer";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "time":
            case "date":
            case "datetime":
            case "datetime2":
                return "LocalDateTime";
            case "blob":
            case "longblob":
            case "binary":
            case "varbinary":
                return "byte[]";
            default:
                return null;
        }
    }
}
