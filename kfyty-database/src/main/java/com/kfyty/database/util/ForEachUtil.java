package com.kfyty.database.util;

import com.kfyty.database.jdbc.annotation.ForEach;
import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.CommonUtil;

import java.util.List;
import java.util.Map;

import static com.kfyty.core.utils.CommonUtil.EMPTY_STRING;

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
            List<?> list = CommonUtil.toList(params.get(each.collection()).getValue());
            builder.append(each.open());
            for (int i = 0; i < list.size(); i++) {
                String flag = "param_" + i + "_";
                Object value = list.get(i);
                builder.append(each.sql().replace("#{", "#{" + flag).replace("${", "${" + flag));
                params.put(flag + each.item(), new MethodParameter(value == null ? Object.class : value.getClass(), value, flag + each.item()));
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
