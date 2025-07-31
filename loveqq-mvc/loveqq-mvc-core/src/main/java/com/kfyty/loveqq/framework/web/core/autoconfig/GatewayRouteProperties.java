package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.RefreshScope;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayFilter;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayPredicate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@RefreshScope
@ConfigurationProperties("k.server.gateway")
public class GatewayRouteProperties {
    /**
     * 路由
     */
    private List<RouteDefinition> routes;

    /**
     * 字符串到 {@link Predicates} 转换器，以支持简易配置
     * eg: Path=/api/demo/**，该形式是最简易的形式
     * eg: Path=path=/api/demo/**&extra=1，该形式为多参数简易形式
     */
    @Component
    public static class StringToPredicatesConverter implements Converter<String, Predicates> {

        @Override
        public Predicates apply(String source) {
            int equalsIndex = source.indexOf('=');
            String id = source.substring(0, equalsIndex);
            String argsStr = source.substring(equalsIndex + 1);

            if (argsStr.indexOf('=') > -1) {
                return Predicates.builder()
                        .id(id)
                        .args(CommonUtil.resolveURLParameters(argsStr))
                        .build();
            }

            Map<String, String> args = new HashMap<>(4);
            args.put(Character.toLowerCase(id.charAt(0)) + id.substring(1), argsStr);

            return Predicates.builder()
                    .id(id)
                    .args(args)
                    .build();
        }
    }

    /**
     * 字符串到 {@link Filters} 转换器，以支持简易配置
     * eg: StripPrefix=1，该形式是最简易的形式
     * eg: StripPrefix=stripPrefix=1&extra=1，该形式为多参数简易形式
     */
    @Component
    public static class StringToFiltersConverter implements Converter<String, Filters> {

        @Override
        public Filters apply(String source) {
            int equalsIndex = source.indexOf('=');
            String id = source.substring(0, equalsIndex);
            String argsStr = source.substring(equalsIndex + 1);

            if (argsStr.indexOf('=') > -1) {
                return Filters.builder()
                        .id(id)
                        .args(CommonUtil.resolveURLParameters(argsStr))
                        .build();
            }

            Map<String, String> args = new HashMap<>(4);
            args.put(Character.toLowerCase(id.charAt(0)) + id.substring(1), argsStr);

            return Filters.builder()
                    .id(id)
                    .args(args)
                    .build();
        }
    }

    @Data
    public static class RouteDefinition {
        /**
         * 路由 uri
         */
        private String uri;

        /**
         * 路由断言
         */
        private List<Predicates> predicates;

        /**
         * 路由过滤器
         */
        private List<Filters> filters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Predicates {
        /**
         * 要应用的断言id
         * 一般是 {@link GatewayPredicate} 实例的 bean 名称
         */
        private String id;

        /**
         * 断言参数
         * 会绑定到对应的 {@link GatewayPredicate} 的配置类，需按配置字段名称配置
         */
        private Map<String, String> args;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filters {
        /**
         * 要应用的过滤器id
         * 一般是 {@link GatewayFilter} 实例的 bean 名称
         */
        private String id;

        /**
         * 过滤器参数
         */
        private Map<String, String> args;
    }
}
