package com.kfyty.database.generator.config;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.JdbcTypeUtil;
import com.kfyty.database.generator.info.AbstractFieldStructInfo;
import com.kfyty.database.jdbc.intercept.InterceptorChain;
import com.kfyty.database.jdbc.intercept.QueryInterceptor;

import java.util.Collection;
import java.util.List;

import static com.kfyty.core.utils.CommonUtil.mapping;

/**
 * 描述: 字段信息拦截器
 *
 * @author kfyty725
 * @date 2021/9/17 18:47
 * @email kfyty725@hotmail.com
 */
public class FieldStructInfoInterceptor implements QueryInterceptor {

    @Override
    public Object intercept(Object retValue, List<MethodParameter> params, InterceptorChain chain) {
        return retValue instanceof Collection ? mapping(retValue, e -> {
            if (e instanceof AbstractFieldStructInfo) {
                AbstractFieldStructInfo info = (AbstractFieldStructInfo) e;
                info.setJdbcType(JdbcTypeUtil.convert2JdbcType(info.getFieldType()));
            }
            return e;
        }) : retValue;
    }
}
