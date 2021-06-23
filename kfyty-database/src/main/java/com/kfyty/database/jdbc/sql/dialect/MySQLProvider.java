package com.kfyty.database.jdbc.sql.dialect;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.sql.InsertAllProvider;
import com.kfyty.database.jdbc.sql.Provider;
import com.kfyty.database.jdbc.sql.ProviderAdapter;
import com.kfyty.database.jdbc.sql.UpdateAllProvider;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import javafx.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/8 10:52
 * @email kfyty725@hotmail.com
 */
public class MySQLProvider extends ProviderAdapter implements InsertAllProvider, UpdateAllProvider {

    @Override
    public String doProviderInsertAll(Class<?> mapperClass, Method sourceMethod, Execute annotation) {
        Class<?> entityClass = ReflectUtil.getSuperGeneric(mapperClass, 1);
        Pair<String, String> fieldPair = this.buildInsertField(entityClass);
        String baseSQL = String.format("insert into %s (%s) values ", CommonUtil.camelCase2Underline(entityClass.getSimpleName()), fieldPair.getKey());
        ReflectUtil.setAnnotationValue(annotation, "value", baseSQL);
        ReflectUtil.setAnnotationValue(annotation, "forEach", new ForEach[] {
                new ForEach() {
                    @Override
                    public String collection() {
                        return Provider.PROVIDER_PARAM_ENTITY;
                    }

                    @Override
                    public String item() {
                        return "item";
                    }

                    @Override
                    public String open() {
                        return "(";
                    }

                    @Override
                    public String sqlPart() {
                        return fieldPair.getValue().replace(Provider.PROVIDER_PARAM_ENTITY, item());
                    }

                    @Override
                    public String separator() {
                        return "), (";
                    }

                    @Override
                    public String close() {
                        return ")";
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ForEach.class;
                    }
                }
        });
        return baseSQL;
    }

    @Override
    public String doProviderUpdateAll(Class<?> mapperClass, Method sourceMethod, Execute annotation) {
        String updateSQL = buildUpdateSQL(mapperClass);
        ReflectUtil.setAnnotationValue(annotation, "forEach", new ForEach[] {
                new ForEach() {
                    @Override
                    public String collection() {
                        return Provider.PROVIDER_PARAM_ENTITY;
                    }

                    @Override
                    public String item() {
                        return "item";
                    }

                    @Override
                    public String open() {
                        return "";
                    }

                    @Override
                    public String sqlPart() {
                        return updateSQL.replace(Provider.PROVIDER_PARAM_ENTITY, item());
                    }

                    @Override
                    public String separator() {
                        return ";";
                    }

                    @Override
                    public String close() {
                        return "";
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ForEach.class;
                    }
                }
        });
        return "";
    }
}
