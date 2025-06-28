<h1 align="center" style="text-align:center;">
    <img src="loveqq.png" width="100"  alt=""/>
    <p align="center" style="font-size: 20px">
      loveqq-framework，全新轻量级 ioc/aop/javafx 框架，更小，更强大
    </p>
</h1>

# loveqq-framework
    全新轻量级 ioc/aop/javafx 框架，更小，更强大。
    该框架基本实现自我配置，具有更强大的复杂的条件bean注册推断，全框架复合注解支持。
    提供抽象 mvc 模式，统一命令式/响应式编程风格，包含过滤器、拦截器等，提供嵌入式 reactor-netty、tomcat 服务器。
    提供 javafx mvvm 框架，可实现模型-数据的双向绑定，父子窗口生命周期绑定及监听。
    提供动态数据源配置支持；提供注解式缓存支持。
    已集成 
        aspectj、reactor-netty、tomcat、nacos、feign、mybatis、spring-tx、jsr303-valid、
        logback、jakarta-mail、quartz、xxl-job、pagehelper、redisson、shiro、thymeleaf、
        mybatis-flex、sa-token、baidu-uid-generator 等常用组件。
    默认提供 jar 包瘦身方式打包，支持 jar-index 启动，启动速度更快。
    已提供 Ruoyi-for-loveqq 版本，替换掉了 spring 及其全部 starter，开箱即用。

## loveqq-core
项目核心，包含一些通用工具类、jar index 启动引导、泛型推断工具、jdbc 访问工具、bean 封装工具、包读取工具、json 转换工具以及自动配置接口、自动配置注解等
详情请点击 [loveqq-core 模块 README.md.](./loveqq-core/README.md)

## loveqq-boot
ioc 容器具体实现、自动装配、作用域代理(单例/原型/刷新)、懒加载代理、配置文件属性自动绑定(支持嵌套的复杂类型绑定)、条件注解、jsr 条件注解校验器、异步事件、动态代理、spi、自定义 jar index 类加载器等。
详情请点击 [loveqq-boot 模块 README.md.](./loveqq-boot/README.md)

## loveqq-aop
aop 模块，支持 ant 路径匹配、注解类型匹配、集成 AspectJ 支持 pointcut 表达式匹配。
详情请点击 [loveqq-aop 模块 README.md.](./loveqq-aop/README.md)

## loveqq-boot-starter-datasource
数据源启动器，支持 HikariCP、druid、tomcat-jdbc 的自动配置，支持动态数据源
详情请点击 [loveqq-boot-starter-datasource 模块 README.md.](./loveqq-boot-starter-datasource/README.md)

## loveqq-data-korm
数据库访问工具，基于代理实现了接口式 jdbc 访问，支持注解编写 SQL 或者基于模板引擎(如：enjoy/freemarker) 的动态 SQL。

## loveqq-data-codegen
代码生成器和默认生成模板，支持 java 编程模板、enjoy/freemarker 模板以及 jsp 模板（需要 k-jte 支持），或者自定义模板引擎。

## loveqq-mvc-core
基础 mvc 抽象，用于适配不同的 web 服务器，包含了路由注册、参数绑定、请求分发、请求拦截器等。
详情请点击 [loveqq-mvc-core 模块 README.md.](./loveqq-mvc/loveqq-mvc-core/README.md)

## loveqq-mvc-servlet
mvc 模式的 servlet 实现，主要实现了 DispatcherServlet，以及 Filter、Servlet 的自动配置等。
配套的启动器是 loveqq-boot-starter-tomcat。详情请点击 [loveqq-mvc-servlet 模块 README.md.](./loveqq-mvc/loveqq-mvc-servlet/README.md)

## loveqq-mvc-netty
mvc 模式的 netty 实现，主要实现了 DispatcherHandler，并添加自定义 Filter 的自动配置等。
配套的启动器是 loveqq-boot-starter-netty。详情请点击 [loveqq-mvc-netty 模块 README.md.](./loveqq-mvc/loveqq-mvc-netty/README.md)

## loveqq-cache-core
loveqq 缓存基础抽象，默认基于内存实现。支持缓存注解，统一命令式/响应式缓存注解使用方式
详情请点击 [loveqq-cache-core 模块 README.md.](./loveqq-cache/loveqq-cache-core/README.md)

## loveqq-cache-redis
loveqq 缓存基于 redis 的实现

## loveqq-boot-cloud-bootstrap
微服务架构必须的引导启动模块

## loveqq-boot-starter-validator
jsr303-valid 集成，并添加了自定义约束注解，可实现基于 Ognl 表达式的动态联动校验。

## loveqq-boot-starter-discovery-nacos/loveqq-boot-starter-config-nacos
集成了 nacos 服务发现、服务配置

## loveqq-boot-starter-dubbo
集成了 dubbo 自动配置

## loveqq-boot-starter-feign
集成了 feign 自动配置，可配合注册中心使用，支持 loveqq-mvc 注解

## loveqq-boot-starter-rocketmq
集成了 rocketmq 自动配置

## loveqq-boot-starter-redisson
集成了基于 redisson 的 redis 支持，同时内置了基于 redis 的简易 mq 实现

## loveqq-sdk
用于快速封装 sdk，只需编写请求及响应模型，支持拦截器，易于拓展，支持命令式、响应式风格

## loveqq-javafx
javafx mvvm 框架，实现了视图和数据模型的双向绑定。代码中只需操作数据即可反应到视图上，视图上编辑数据即可反应到模型里。
详情请点击 [loveqq-javafx 模块 README.md.](./loveqq-javafx/README.md)

## Ruoyi-for-loveqq
基于 loveqq 框架的单体版若依，去除了底层 spring 及其 spring boot starter。详情请查看仓库列表。

### 示例
```java
package com.kfyty.demo;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.boot.validator.annotation.Condition;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Async;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import com.kfyty.loveqq.framework.web.core.annotation.GetMapping;
import com.kfyty.loveqq.framework.web.core.autoconfig.annotation.EnableWebMvc;
import lombok.Data;

@Async
@EnableWebMvc
@EventListener
@BootApplication
public class Main {

    public static void main(String[] args) {
        K.run(Main.class, args);
    }

    @Cacheable
    @GetMapping
    public User hello(@Valid User user) {
        return user;
    }

    @Async
    @EventListener
    public void onStarted(ContextRefreshedEvent event) {
        log.info("started succeed !");
    }

    @Data
    public static class User {
        @Condition(when = "type == 1", then = "photo != null", message = "type=1时，图片不能为空")
        private Integer type;

        private String photo;
    }
}
```

## 最佳实践
### maven
建议项目继承 loveqq-framework 父模块
```xml
<parent>
    <groupId>com.kfyty</groupId>
    <artifactId>loveqq-framework</artifactId>
    <version>1.1.3-M2</version>
</parent>

<dependencies>
    <dependency>
        <groupId>com.kfyty</groupId>
        <artifactId>loveqq-boot</artifactId>
        <version>${loveqq.framework.version}</version>
    </dependency>
    
    <dependency>
        <groupId>com.kfyty</groupId>
        <artifactId>loveqq-boot-starter-logback</artifactId>
        <version>${loveqq.framework.version}</version>
    </dependency>
</dependencies>
```

打包时，需要在子模块添加以下配置，以设置启动类，并生成 jar index
```xml
<properties>
    <boot-start-class>com.kfyty.demo.Main</boot-start-class>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```
### gradle
需要添加以下依赖
```groovy
apply plugin: 'java'

group = 'com.kfyty.example'
version = '1.0-SNAPSHOT'

ext {
    bootLibOutput = 'boot-lib'
    bootMainClass = 'com.kfyty.loveqq.framework.core.support.BootLauncher'
    bootStartClass = 'com.kfyty.demo.Main'
}

dependencies {
    implementation 'com.kfyty:loveqq-framework:1.1.3-M2@pom'
    implementation 'com.kfyty:loveqq-boot:1.1.3-M2'
    implementation 'com.kfyty:loveqq-boot-starter-logback:1.1.3-M2'
    implementation 'org.ow2.asm:asm:9.5'
    implementation 'org.javassist:javassist:3.29.0-GA'
    compileOnly "org.projectlombok:lombok:1.18.30"
    annotationProcessor "org.projectlombok:lombok:1.18.30"
}

allprojects {
    compileJava {
        options.encoding = "UTF-8"
    }
}
```

打包时，需要在子模块添加以下配置，以设置启动类，并生成 jar index
```groovy
// 复制依赖
tasks.register('copyDependencies', Copy) {
    from configurations.runtimeClasspath
    into "$buildDir/libs/$rootProject.ext.bootLibOutput"
}

// 构建 jar index
tasks.register('buildJarIndex', JavaExec) {
    mainClass = 'com.kfyty.loveqq.framework.core.support.task.BuildJarIndexAntTask'
    classpath = configurations.runtimeClasspath
    args "-OUTPUT_DIRECTORY=$project.buildDir/libs"
    args "-OUTPUT_JAR=$project.name-$project.version" + '.jar'
    args "-OUTPUT_DEFAULT_JAR=$project.name-$project.version" + '.jar'
}

// jar，覆盖默认 jar
jar {
    dependsOn copyDependencies

    manifest {
        attributes 'Main-Class': "$rootProject.ext.bootMainClass"
        attributes 'Start-Class': "$rootProject.ext.bootStartClass"
        attributes 'Class-Path': configurations.runtimeClasspath.files.collect { "$rootProject.ext.bootLibOutput/$it.name" }.join(' ')
    }
}

// build 覆盖默认 build
build {
    dependsOn buildJarIndex
}
```

### docker

创建 Dockerfile
```dockerfile
FROM docker.m.daocloud.io/openjdk:17

ENV TZ=Asia/Shanghai

WORKDIR /app/demo

EXPOSE 9090

COPY ./target/demo-1.0-SNAPSHOT.jar /app/demo/demo-1.0-SNAPSHOT.jar
COPY ./target/boot-lib /app/demo/boot-lib

ENTRYPOINT ["java", "--add-opens=java.base/sun.reflect.annotation=ALL-UNNAMED", "--add-opens=java.base/java.io=ALL-UNNAMED", "-jar", "demo-1.0-SNAPSHOT.jar"]

# -t 指定镜像名称:标签
# -f 指定 Dockerfile, 如果改名的话
# . 表示 Dockerfile 在当前目录
# docker build -t demo:1.0 -f Dockerfile .

# -p 表示端口映射，将 9090(容器对外端口) 转发到 8080(容器内端口即应用实际的被docker管理的端口)
# docker run -p 9090:8080 demo:1.0
```
