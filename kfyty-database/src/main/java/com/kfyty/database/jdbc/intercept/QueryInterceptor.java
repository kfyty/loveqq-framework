package com.kfyty.database.jdbc.intercept;

import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.ResultSetUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/8/8 13:30
 * @email kfyty725@hotmail.com
 */
public interface QueryInterceptor extends Interceptor {

    @Override
    default Object intercept(PreparedStatement ps, SimpleGeneric returnType, MethodParameter... params) throws SQLException {
        return this.intercept(ps, ps.executeQuery(), returnType, params);
    }

    default Object intercept(PreparedStatement ps, ResultSet rs, SimpleGeneric returnType, MethodParameter ... params) throws SQLException {
        return this.intercept(ps, ResultSetUtil.processObject(rs, returnType), params);
    }

    Object intercept(PreparedStatement ps, Object retValue, MethodParameter ... params) throws SQLException;
}
