package com.kfyty.database.jdbc.sql.dynamic;

import com.kfyty.database.jdbc.mapping.TemplateStatement;
import com.kfyty.database.jdbc.session.Configuration;
import com.kfyty.core.method.MethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 描述: 动态 SQL 提供者
 *
 * @author kfyty725
 * @date 2021/9/29 22:44
 * @email kfyty725@hotmail.com
 */
public interface DynamicProvider<TS extends TemplateStatement> {
    /**
     * 设置全局配置
     *
     * @param configuration 配置
     */
    void setConfiguration(Configuration configuration);

    /**
     * 根据给出的路径，解析出动态 SQL 模板集合
     *
     * @param paths 路径
     * @return TemplateStatement
     */
    List<TS> resolve(List<String> paths);

    /**
     * 渲染给定的动态 SQL 模板
     *
     * @param template TemplateStatement
     * @param params   模板参数
     * @return 渲染后的 SQL
     */
    String processTemplate(TS template, Map<String, Object> params);

    /**
     * 根据给定的接口解析动态 SQL id
     *
     * @param mapperClass  mapper class
     * @param mapperMethod 代理方法
     * @return id
     */
    String resolveTemplateStatementId(Class<?> mapperClass, Method mapperMethod);

    /**
     * 提供动态 SQL 入口
     *
     * @param mapperClass  mapper class
     * @param mapperMethod 代理方法
     * @param params       方法参数
     * @return SQL
     */
    String doProvide(Class<?> mapperClass, Method mapperMethod, Map<String, MethodParameter> params);

    /**
     * 提供动态 SQL 入口
     *
     * @param mapperClass  mapper class
     * @param mapperMethod 代理方法
     * @param annotation   方法注解
     * @param params       方法参数
     * @return SQL
     */
    default String doProvide(Class<?> mapperClass, Method mapperMethod, Annotation annotation, Map<String, MethodParameter> params) {
        return this.doProvide(mapperClass, mapperMethod, params);
    }
}
