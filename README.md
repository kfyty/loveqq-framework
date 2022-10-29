# kfyty-utils
    造轮子~~ 注解式 ioc/aop、自动装配、异步事件、动态代理、注解式 mvc（支持 Restful）、嵌入式 tomcat、注解式 jdbc 框架，并基于此开发了一套支持 java/freemarker/jsp 模板的代码生成器。

## kfyty-core
项目核心，包含一些通用工具类、泛型推断工具、jdbc 访问工具、bean 封装工具、包读取工具、json 转换工具以及自动配置接口、自动配置注解等

## kfyty-database
数据库访问工具，基于代理实现了接口式 jdbc 访问，支持注解编写 SQL 或者基于模板引擎(如：freemarker) 的动态 SQL；
内置代码生成器和默认生成模板，支持 java 编程模板、freemarker 模板以及 jsp 模板（需要 k-jte 支持），或者自定义模板引擎。

## kfyty-mvc
注解式 mvc，支持嵌入式 tomcat、文件上传接口、 restful 风格 url 解析匹配等。

## kfyty-aop
集成 AspectJ 实现注解式 aop，可单独运行，也可结合 kfyty-boot 自动运行。 

## kfyty-sdk
用于快速封装 sdk，只需编写请求及响应模型，支持拦截器，易于拓展。

## kfyty-excel
基于 xml 解析，实现了处理数据和导出 excel 同时进行。

## kfyty-boot
注解式 ioc、自动装配、作用域(单例/原型/刷新)、条件注解、异步事件、动态代理、spi 等。
```xml
<dependency>
    <groupId>com.kfyty</groupId>
    <artifactId>kfyty-boot</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
```java
package com.kfyty.demo;

import com.kfyty.boot.K;
import com.kfyty.core.autoconfig.annotation.Async;
import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.autoconfig.annotation.EventListener;
import com.kfyty.core.event.ContextRefreshedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@BootApplication
public class Main {

    public static void main(String[] args) {
        K.run(Main.class, args);
    }

    @Async
    @EventListener
    public void onStarted(ContextRefreshedEvent event) {
        log.info("started succeed !");
    }
}
```
