## loveqq-mvc-core

### 通用接口
* web 服务器：com.kfyty.loveqq.framework.web.core.WebServer
* 基础请求分发器：com.kfyty.loveqq.framework.web.core.AbstractDispatcher
* 响应式基础请求分发器：com.kfyty.loveqq.framework.web.core.AbstractReactiveDispatcher
* 请求映射解析器：com.kfyty.loveqq.framework.web.core.handler.RequestMappingHandler
* 路由匹配器：com.kfyty.loveqq.framework.web.core.route.RouteRegistry
* 异常处理器：com.kfyty.loveqq.framework.web.core.handler.ExceptionHandler
* 处理器方法参数解析器：com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver
* 处理器方法返回值处理器：com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor
* 统一抽象 http 请求：com.kfyty.loveqq.framework.web.core.http.ServerRequest
* 统一抽象 http 响应：com.kfyty.loveqq.framework.web.core.http.ServerResponse
* 请求线程上下文：com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder
* 响应线程上下文：com.kfyty.loveqq.framework.web.core.request.support.ResponseContextHolder
* 上传文件抽象接口：com.kfyty.loveqq.framework.web.core.multipart.MultipartFile
* 请求拦截器：com.kfyty.loveqq.framework.web.core.interceptor.HandlerInterceptor
* 响应式请求拦截器：com.kfyty.loveqq.framework.web.core.interceptor.ReactiveHandlerInterceptor
* cors 跨域配置：com.kfyty.loveqq.framework.web.core.cors.CorsConfiguration
* 相关注解可查看包：com.kfyty.loveqq.framework.web.core.annotation.**

### 主要配置示例
```yml
k:
  server:
    port: 8080                                  # 端口
    virtualThread: false                        # 是否启用虚拟线程
    maxThreads: 100                             # 最大线程数
    # 下面两个配置是静态资源处理配置
    # 注意：tomcat 和 netty 服务器需要配置的可能不一样，因为 tomcat 是内置匹配逻辑，netty 是基于 ant 路径匹配逻辑
    staticPattern:                              # 项目中的静态资源路径
      - /static/*
      - *.png
    resources:                                  # 本地静态资源路径
      - key: /resource/*.png                    # uri
        value: /user/project/resource/          # 本地磁盘基础路径
```