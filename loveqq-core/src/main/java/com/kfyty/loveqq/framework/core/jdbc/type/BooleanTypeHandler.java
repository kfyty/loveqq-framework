package com.kfyty.loveqq.framework.core.jdbc.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 11:17
 * @email kfyty725@hotmail.com
 */
public class BooleanTypeHandler implements TypeHandler<Boolean> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(boolean.class);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Boolean parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.BOOLEAN);
        } else {
            ps.setBoolean(i, parameter);
        }
    }

    @Override
    public Boolean getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getBoolean(columnName);
    }
}
