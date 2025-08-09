package com.kfyty.loveqq.framework.web.core.route.gateway;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: 网关路由定义
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @see com.kfyty.loveqq.framework.web.core.route.GatewayRoute#create(BeanFactory, RouteDefinition)
 * @since JDK 1.8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDefinition {
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

    /**
     * 排序
     */
    private Integer order;

    /**
     * 返回一个构建器
     *
     * @return 构建器
     */
    public static RouteDefinitionBuilder builder() {
        return new RouteDefinitionBuilder();
    }

    @Data
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

        /**
         * 返回一个构建器
         *
         * @return 构建器
         */
        public static PredicatesBuilder builder() {
            return new PredicatesBuilder();
        }

        /**
         * 构建器
         */
        public static class PredicatesBuilder {
            /**
             * target
             */
            private final Predicates predicates;

            public PredicatesBuilder() {
                this.predicates = new Predicates();
            }

            public PredicatesBuilder id(String id) {
                this.predicates.setId(id);
                return this;
            }

            public PredicatesBuilder args(Map<String, String> args) {
                this.predicates.setArgs(args);
                return this;
            }

            public PredicatesBuilder args(String key, String value) {
                if (this.predicates.getArgs() == null) {
                    this.predicates.setArgs(new HashMap<>(4));
                }
                this.predicates.getArgs().put(key, value);
                return this;
            }

            public Predicates build() {
                return this.predicates;
            }
        }
    }

    @Data
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

        /**
         * 返回一个构建器
         *
         * @return 构建器
         */
        public static FiltersBuilder builder() {
            return new FiltersBuilder();
        }

        /**
         * 构建器
         */
        public static class FiltersBuilder {
            /**
             * target
             */
            private final Filters filters;

            public FiltersBuilder() {
                this.filters = new Filters();
            }

            public FiltersBuilder id(String id) {
                this.filters.setId(id);
                return this;
            }

            public FiltersBuilder args(Map<String, String> args) {
                this.filters.setArgs(args);
                return this;
            }

            public FiltersBuilder args(String key, String value) {
                if (this.filters.getArgs() == null) {
                    this.filters.setArgs(new HashMap<>(4));
                }
                this.filters.getArgs().put(key, value);
                return this;
            }

            public Filters build() {
                return this.filters;
            }
        }
    }

    /**
     * 构建器
     */
    public static class RouteDefinitionBuilder {
        /**
         * target
         */
        private final RouteDefinition routeDefinition;

        public RouteDefinitionBuilder() {
            this.routeDefinition = new RouteDefinition();
        }

        public RouteDefinitionBuilder uri(String uri) {
            this.routeDefinition.setUri(uri);
            return this;
        }

        public RouteDefinitionBuilder predicates(List<Predicates> predicates) {
            this.routeDefinition.setPredicates(predicates);
            return this;
        }

        public RouteDefinitionBuilder filters(List<Filters> filters) {
            this.routeDefinition.setFilters(filters);
            return this;
        }

        public RouteDefinitionBuilder predicate(Predicates predicates) {
            if (this.routeDefinition.getPredicates() == null) {
                this.routeDefinition.setPredicates(new LinkedList<>());
            }
            this.routeDefinition.getPredicates().add(predicates);
            return this;
        }

        public RouteDefinitionBuilder filter(Filters filters) {
            if (this.routeDefinition.getFilters() == null) {
                this.routeDefinition.setFilters(new LinkedList<>());
            }
            this.routeDefinition.getFilters().add(filters);
            return this;
        }

        public RouteDefinitionBuilder order(Integer order) {
            this.routeDefinition.setOrder(order);
            return this;
        }

        public RouteDefinition build() {
            return this.routeDefinition;
        }
    }
}
