package com.kfyty.database.util;

import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.support.method.MethodParameter;
import com.kfyty.support.utils.CommonUtil;

import java.util.List;
import java.util.Map;

import static com.kfyty.support.utils.CommonUtil.EMPTY_STRING;

/**
 * 描述: ForEach 注解处理工具
 *
 * @author kfyty725
 * @date 2021/9/30 14:16
 * @email kfyty725@hotmail.com
 */
public abstract class ForEachUtil {

    public static String processForEach(Map<String, MethodParameter> params, ForEach... forEachList) {
        if (CommonUtil.empty(forEachList)) {
            return EMPTY_STRING;
        }
        StringBuilder builder = new StringBuilder();
        for (ForEach each : forEachList) {
            MethodParameter parameter = params.get(each.collection());
            List<Object> list = CommonUtil.toList(parameter.getValue());
            builder.append(each.open());
            for (int i = 0; i < list.size(); i++) {
                String flag = "param_" + i + "_";
                Object value = list.get(i);
                builder.append(each.sqlPart().replace("#{", "#{" + flag).replace("${", "${" + flag));
                params.put(flag + each.item(), new MethodParameter(value == null ? Object.class : value.getClass(), value));
                if (i == list.size() - 1) {
                    break;
                }
                builder.append(each.separator());
            }
            builder.append(each.close());
        }
        return builder.toString();
    }
}
