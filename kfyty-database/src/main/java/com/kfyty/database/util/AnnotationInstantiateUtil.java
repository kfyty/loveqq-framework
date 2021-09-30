package com.kfyty.database.util;

import com.kfyty.database.jdbc.annotation.Execute;
import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.database.jdbc.annotation.SubQuery;
import com.kfyty.database.jdbc.sql.dynamic.DynamicProvider;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static com.kfyty.database.jdbc.session.Configuration.SELECT_LABEL;
import static com.kfyty.support.utils.CommonUtil.EMPTY_STRING;

/**
 * 描述: 注解实例化工具
 *
 * @author kfyty725
 * @date 2021/7/22 18:53
 * @email kfyty725@hotmail.com
 */
public abstract class AnnotationInstantiateUtil {

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
                return new ForEach[0];
            }

            @Override
            public SubQuery[] subQuery() {
                return new SubQuery[0];
            }

            @Override
            public Class<?> provider() {
                return DynamicProvider.class;
            }

            @Override
            public String method() {
                return null;
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
                return new ForEach[0];
            }

            @Override
            public Class<?> provider() {
                return DynamicProvider.class;
            }

            @Override
            public String method() {
                return null;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Execute.class;
            }
        };
    }

    public static ForEach createForEach(String collection, String open, String separator, String close, Function<ForEach, String> sqlPart) {
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
            public String sqlPart() {
                return sqlPart.apply(this);
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
