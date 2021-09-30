package com.kfyty.database.jdbc.sql.dynamic;

import com.kfyty.database.jdbc.mapping.TemplateStatement;
import com.kfyty.database.jdbc.session.Configuration;
import com.kfyty.support.method.MethodParameter;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 动态 SQL 提供基础实现
 *
 * @author kfyty725
 * @date 2021/9/30 13:13
 * @email kfyty725@hotmail.com
 */
@Data
public abstract class AbstractDynamicProvider<TS extends TemplateStatement> implements DynamicProvider<TS> {
    private Configuration configuration;

    @Override
    public String resolveTemplateStatementId(Class<?> mapperClass, Method mapperMethod) {
        String id = mapperClass.getName() + "." + mapperMethod.getName();
        if (!this.configuration.getTemplateStatements().containsKey(id)) {
            throw new IllegalArgumentException("template statement not exists of id: " + id);
        }
        return id;
    }

    @Override
    public String doProvide(Class<?> mapperClass, Method mapperMethod, Annotation annotation, Map<String, MethodParameter> params) {
        String id = this.resolveTemplateStatementId(mapperClass, mapperMethod);
        Map<String, Object> parameters = this.processTemplateParameters(params);
        TemplateStatement templateStatement = this.configuration.getTemplateStatements().get(id);
        return this.processTemplate((TS) templateStatement, parameters);
    }

    protected Map<String, Object> processTemplateParameters(Map<String, MethodParameter> methodParameterMap) {
        Map<String, Object> params = new HashMap<>(methodParameterMap.size());
        for (Map.Entry<String, MethodParameter> entry : methodParameterMap.entrySet()) {
            params.put(entry.getKey(), entry.getValue().getValue());
        }
        return params;
    }
}
