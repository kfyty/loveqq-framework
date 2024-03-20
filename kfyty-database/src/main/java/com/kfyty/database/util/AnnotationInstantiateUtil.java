package com.kfyty.database.util;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.SubQuery;
import com.kfyty.database.jdbc.sql.dynamic.DynamicProvider;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static com.kfyty.database.jdbc.session.Configuration.SELECT_LABEL;
import static com.kfyty.core.utils.CommonUtil.EMPTY_STRING;

/**
 * 描述: 注解实例化工具
 *
 * @author kfyty725
 * @date 2021/7/22 18:53
 * @email kfyty725@hotmail.com
 */
public abstract class AnnotationInstantiateUtil {
    private static final ForEach[] EMPTY_FOR_EACH_ARRAY = new ForEach[0];
    private static final SubQuery[] EMPTY_SUB_QUERY_ARRAY = new SubQuery[0];

    public static Annotation createDynamicByLabelType(String labelType) {
        return SELECT_LABEL.equals(labelType) ? createDynamicQuery() : createDynamicExecute();
    }

    public static Query createDynamicQuery() {
        return new Query() {

            @Override
            public String value() {
                return EMPTY_STRING;
            }

            @Override
            public String key() {
                return null;
            }

            @Override
            public ForEach[] forEach() {
                return EMPTY_FOR_EACH_ARRAY;
            }

            @Override
            public SubQuery[] subQuery() {
                return EMPTY_SUB_QUERY_ARRAY;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public Class<DynamicProvider> provider() {
                return DynamicProvider.class;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Query.class;
            }
        };
    }

    public static Execute createDynamicExecute() {
        return new Execute() {

            @Override
            public String value() {
                return EMPTY_STRING;
            }

            @Override
            public ForEach[] forEach() {
                return EMPTY_FOR_EACH_ARRAY;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public Class<DynamicProvider> provider() {
                return DynamicProvider.class;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Execute.class;
            }
        };
    }

    public static ForEach createForEach(String collection, String open, String separator, String close, Function<ForEach, String> sql) {
        return new ForEach() {

            @Override
            public String collection() {
                return collection;
            }

            @Override
            public String item() {
                return "item";
            }

            @Override
            public String open() {
                return open;
            }

            @Override
            public String sql() {
                return sql.apply(this);
            }

            @Override
            public String separator() {
                return separator;
            }

            @Override
            public String close() {
                return close;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ForEach.class;
            }
        };
    }
}
