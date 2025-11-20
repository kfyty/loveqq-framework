package com.kfyty.loveqq.framework.web.core.route.gateway.converter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.route.gateway.RouteDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * 字符串到 {@link RouteDefinition.Predicates} 转换器，以支持简易配置
 * eg: Path=/api/demo/**，该形式是最简易的形式
 * eg: Path=path=/api/demo/**&extra=1，该形式为多参数简易形式
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Component
public class StringToPredicatesConverter implements Converter<String, RouteDefinition.Predicates> {

    @Override
    public RouteDefinition.Predicates apply(String source) {
        int equalsIndex = source.indexOf('=');
        String id = source.substring(0, equalsIndex);
        String argsStr = source.substring(equalsIndex + 1);

        if (argsStr.indexOf('=') > -1) {
            return RouteDefinition.Predicates.builder()
                    .id(id)
                    .args(CommonUtil.resolveURLParameters(argsStr))
                    .build();
        }

        Map<String, String> args = new HashMap<>(4);
        args.put(Character.toLowerCase(id.charAt(0)) + id.substring(1), argsStr);

        return RouteDefinition.Predicates.builder()
                .id(id)
                .args(args)
                .build();
    }
}