package com.kfyty.loveqq.framework.web.core.route.gateway.converter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.web.core.route.gateway.RouteDefinition;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字符串到 {@link RouteDefinition.Predicates} 转换器，以支持全配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Component
@RequiredArgsConstructor
public class MapToPredicatesConverter implements Converter<Map<String, String>, RouteDefinition.Predicates> {
    private final StringToPredicatesConverter converter;

    @Override
    public RouteDefinition.Predicates apply(Map<String, String> source) {
        String id = source.remove("id");
        if (id == null) {
            return this.converter.apply(source.values().iterator().next());
        }
        Map<String, String> args = source.entrySet().stream().collect(Collectors.toMap(k -> k.getKey().substring(5), Map.Entry::getValue));
        return RouteDefinition.Predicates.builder()
                .id(id)
                .args(args)
                .build();
    }
}