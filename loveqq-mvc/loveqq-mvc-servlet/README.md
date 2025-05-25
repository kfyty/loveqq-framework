## loveqq-mvc-serlvet

### 通用接口
* servlet web 服务器：com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer
* 请求分发器：com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet
* 内置跨域过滤器：com.kfyty.loveqq.framework.web.core.cors.CorsFilter
* 请求/响应上下文过滤器：com.kfyty.loveqq.framework.web.mvc.servlet.filter.RequestResponseContextHolderFilter
* 统一抽象 http 请求 servlet 实现：com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerRequest
* 统一抽象 http 响应 servlet 实现：com.kfyty.loveqq.framework.web.mvc.servlet.http.ServletServerResponse
* 请求线程上下文：com.kfyty.loveqq.framework.web.mvc.servlet.request.support.RequestContextHolder
* 响应线程上下文：com.kfyty.loveqq.framework.web.mvc.servlet.request.support.ResponseContextHolder

### 启动器
配套的启动器是 loveqq-boot-starter-tomcat。
详情请点击 [loveqq-boot-starter-tomcat 模块 README.md.](./../../loveqq-boot-starter-tomcat/README.md)

### 嵌入 tomcat 配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         id="WebApp_ID" version="3.1">
    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
    </welcome-file-list>

    <listener>
        <listener-class>com.kfyty.loveqq.framework.web.mvc.servlet.listener.WebMvcAutoConfigListener</listener-class>
    </listener>

    <context-param>
        <param-name>basePackage</param-name>
        <param-value>com.kfyty.example</param-value>
    </context-param>

    <servlet>
        <servlet-name>dispatcherServlet</servlet-name>
        <servlet-class>com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet</servlet-class>

        <init-param>
            <param-name>prefix</param-name>
            <param-value>/WEB-INF/jsp</param-value>
        </init-param>

        <init-param>
            <param-name>suffix</param-name>
            <param-value>.jsp</param-value>
        </init-param>
    </servlet>

    <servlet-mapping>
        <servlet-name>dispatcherServlet</servlet-name>
        <url-pattern>*.do</url-pattern>
    </servlet-mapping>
</web-app>
```