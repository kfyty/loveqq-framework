# kfyty-utils
        一些简单的轮子，包括注解式 ioc、自动装配、注解式 mvc（支持 Restful）、注解式 jdbc 框架，并基于此开发了一套支持 java/freemarker/jsp 模板的代码生成器，以及基于此用来方便的执行一些 SQL 更新/查询任务，或者生成自定义代码/资源。

## kfyty-support
项目支持，包含一些通用工具类、jdbc 访问工具、bean 封装工具、包读取工具、
json 转换工具以及自动配置接口、自动配置注解等

## kfyty-database
数据库访问工具，实现了注解式 jdbc 访问，类似注解式 mybatis。内置代码生成器和默认生成模板，
支持 java 编程模板、freemarker 模板以及 jsp 模板（需要 k-jte 支持）

## kfyty-mvc
实现了简单的注解式 mvc，支持 restful 风格 url 解析匹配

## kfyty-boot
实现了简单的注解式 ioc、自动装配、嵌入式 tomcat 等，目前仅支持单例
