package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.Ordered;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.RefreshScope;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.route.gateway.RouteDefinition;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@RefreshScope
@ConfigurationProperties("k.server.gateway")
public class GatewayRouteProperties implements Ordered {
    /**
     * 路由
     */
    private List<RouteDefinition> routes;

    /**
     * 排序最高，保证刷新时，先刷新路由配置
     *
     * @return 排序
     */
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    /**
     * 字符串到 {@link RouteDefinition.Predicates} 转换器，以支持简易配置
     * eg: Path=/api/demo/**，该形式是最简易的形式
     * eg: Path=path=/api/demo/**&extra=1，该形式为多参数简易形式
     */
    @Component
    public static class StringToPredicatesConverter implements Converter<String, RouteDefinition.Predicates> {

        @Override
        public boolean isBaseDataType() {
            return false;
        }

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

    /**
     * 字符串到 {@link RouteDefinition.Filters} 转换器，以支持简易配置
     * eg: StripPrefix=1，该形式是最简易的形式
     * eg: StripPrefix=stripPrefix=1&extra=1，该形式为多参数简易形式
     */
    @Component
    public static class StringToFiltersConverter implements Converter<String, RouteDefinition.Filters> {

        @Override
        public boolean isBaseDataType() {
            return false;
        }

        @Override
        public RouteDefinition.Filters apply(String source) {
            int equalsIndex = source.indexOf('=');
            String id = source.substring(0, equalsIndex);
            String argsStr = source.substring(equalsIndex + 1);

            if (argsStr.indexOf('=') > -1) {
                return RouteDefinition.Filters.builder()
                        .id(id)
                        .args(CommonUtil.resolveURLParameters(argsStr))
                        .build();
            }

            Map<String, String> args = new HashMap<>(4);
            args.put(Character.toLowerCase(id.charAt(0)) + id.substring(1), argsStr);

            return RouteDefinition.Filters.builder()
                    .id(id)
                    .args(args)
                    .build();
        }
    }
}
