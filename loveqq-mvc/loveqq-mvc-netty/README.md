## loveqq-mvc-netty

### 通用接口
* 非 servlet web 服务器：com.kfyty.loveqq.framework.web.mvc.netty.ServerWebServer
* 请求分发器：com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler
* 过滤器：com.kfyty.loveqq.framework.web.core.filter.Filter
* ws 过滤器：com.kfyty.loveqq.framework.web.core.filter.ws.WsFilter
* 内置跨域过滤器：com.kfyty.loveqq.framework.web.core.cors.CorsFilter
* ws session：com.kfyty.loveqq.framework.web.mvc.netty.ws.Session
* ws 消息处理器：com.kfyty.loveqq.framework.web.mvc.netty.ws.WebSocketHandler
* 统一抽象 http 请求 netty 实现：com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerRequest
* 统一抽象 http 响应 netty 实现：com.kfyty.loveqq.framework.web.mvc.netty.http.NettyServerResponse
* 请求线程上下文：com.kfyty.loveqq.framework.web.mvc.netty.request.support.RequestContextHolder
* 响应线程上下文：com.kfyty.loveqq.framework.web.mvc.netty.request.support.ResponseContextHolder

### 启动器
配套的启动器是 loveqq-boot-starter-netty。
详情请点击 [loveqq-boot-starter-netty 模块 README.md.](./../../loveqq-boot-starter-netty/README.md)