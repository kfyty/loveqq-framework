package com.kfyty.database.util;

import com.kfyty.database.jdbc.annotation.ForEach;

import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * 描述: 创建 ForEach 注解实例工具
 *
 * @author kfyty725
 * @date 2021/7/22 18:53
 * @email kfyty725@hotmail.com
 */
public abstract class ForEachUtil {

    public static ForEach create(String collection, String open, String separator, String close, Function<ForEach, String> sqlPart) {
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
