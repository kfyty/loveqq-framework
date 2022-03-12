package com.kfyty.support.jdbc.type;

import com.kfyty.support.utils.CommonUtil;

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
public class CharacterTypeHandler implements TypeHandler<Character> {

    @Override
    public List<Class<?>> supportTypes() {
        return Collections.singletonList(char.class);
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Character parameter) throws SQLException {
        if(parameter == null) {
            ps.setNull(i, Types.CHAR);
        } else {
            ps.setString(i, parameter.toString());
        }
    }

    @Override
    public Character getResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return CommonUtil.empty(s) ? null : s.charAt(0);
    }
}
