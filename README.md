<h1 align="center" style="text-align:center;">
    <img src="loveqq.png" width="100"  alt=""/>
    <p align="center" style="font-size: 20px">
      loveqq-framework，全新轻量级 ioc/aop/javafx 框架，更小，更强大
    </p>
</h1>

# loveqq-framework
    全新轻量级 ioc/aop/javafx/分布式网关 框架，更小，更强大。
    该框架基本实现自我配置，具有更强大的复杂的条件bean注册推断，全框架复合注解支持，分布式网关支持。
    提供抽象 mvc 模式，统一命令式/响应式编程风格，包含过滤器、拦截器等，提供嵌入式 reactor-netty、tomcat 服务器。
    提供 javafx mvvm 框架，可实现模型-数据的双向绑定，父子窗口生命周期绑定及监听。
    提供动态数据源配置支持；提供注解式缓存支持。
    已集成 
        aspectj、reactor-netty、tomcat、nacos、feign、mybatis、spring-tx、jsr303-valid、
        logback、jakarta-mail、quartz、xxl-job、pagehelper、redisson、shiro、thymeleaf、
        mybatis-flex、sa-token、baidu-uid-generator 等常用组件。
    默认提供 jar 包瘦身方式打包，支持 jar-index 启动，启动速度更快。
    已提供 Ruoyi-for-loveqq 版本，替换掉了 spring 及其全部 starter，开箱即用。

## 集成模块

| 模块名称                                                                 | 功能概述                                                                                                                                                                                                 | 
|----------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| loveqq-core                                                          | 项目核心，包含一些通用工具类、jar index 启动引导、泛型推断工具、jdbc 访问工具、bean 封装工具、包读取工具、json 转换工具以及自动配置接口、自动配置注解等<br/>详情请点击 [loveqq-core 模块 README.md.](./loveqq-core/README.md)                                              |
| loveqq-boot                                                          | ioc 容器具体实现、自动装配、作用域代理(单例/原型/刷新)、懒加载代理、配置文件属性自动绑定(支持嵌套的复杂类型绑定)、条件注解、jsr 条件注解校验器、异步事件、动态代理、spi、自定义 jar index 类加载器等。<br/>详情请点击 [loveqq-boot 模块 README.md.](./loveqq-boot/README.md)                     |
| loveqq-aop                                                           | aop 模块，支持 ant 路径匹配、注解类型匹配、集成 AspectJ 支持 pointcut 表达式匹配。<br/>详情请点击 [loveqq-aop 模块 README.md.](./loveqq-aop/README.md)                                                                                 |
| loveqq-data-korm                                                     | 数据库访问工具，基于代理实现了接口式 jdbc 访问，支持注解编写 SQL 或者基于模板引擎(如：enjoy/freemarker) 的动态 SQL。                                                                                                                          |
| loveqq-data-codegen                                                  | 代码生成器和默认生成模板，支持 java 编程模板、enjoy/freemarker 模板以及 jsp 模板（需要 k-jte 支持），或者自定义模板引擎。                                                                                                                       |
| loveqq-mvc-core                                                      | 基础 mvc 抽象，用于适配不同的 web 服务器，包含了路由注册、参数绑定、请求分发、请求拦截器等。<br/>详情请点击 [loveqq-mvc-core 模块 README.md.](./loveqq-mvc/loveqq-mvc-core/README.md)                                                                |
| loveqq-mvc-servlet                                                   | mvc 模式的 servlet 实现，主要实现了 DispatcherServlet，以及 Filter、Servlet 的自动配置等。<br/>配套的启动器是 loveqq-boot-starter-tomcat。<br/>详情请点击 [loveqq-mvc-servlet 模块 README.md.](./loveqq-mvc/loveqq-mvc-servlet/README.md) |
| loveqq-mvc-netty                                                     | mvc 模式的 netty 实现，主要实现了 DispatcherHandler，并添加自定义 Filter 的自动配置等。<br/>配套的启动器是 loveqq-boot-starter-netty。<br/>详情请点击 [loveqq-mvc-netty 模块 README.md.](./loveqq-mvc/loveqq-mvc-netty/README.md)            |
| loveqq-cache-core                                                    | loveqq 缓存基础抽象，默认基于内存实现。支持缓存注解，统一命令式/响应式缓存注解使用方式<br/>详情请点击 [loveqq-cache-core 模块 README.md.](./loveqq-cache/loveqq-cache-core/README.md)                                                              |
| loveqq-cache-redis                                                   | loveqq 缓存基于 redis 的实现                                                                                                                                                                                |
| loveqq-boot-cloud-bootstrap                                          | 微服务架构必须的引导启动模块                                                                                                                                                                                       |
| loveqq-boot-starter-datasource                                       | 数据源启动器，支持 HikariCP、druid、tomcat-jdbc 的自动配置，支持动态数据源<br/>详情请点击 [loveqq-boot-starter-datasource 模块 README.md.](./loveqq-boot-starter-datasource/README.md)                                              |
| loveqq-boot-starter-tomcat                                           | 集成了 tomcat 自动配置                                                                                                                                                                                      |
| loveqq-boot-starter-netty                                            | 集成了 reactor-netty 自动配置，该启动器支持网关路由，可作为分布式网关使用                                                                                                                                                         |
| loveqq-boot-starter-validator                                        | jsr303-valid 集成，并添加了自定义约束注解，可实现基于 Ognl 表达式的动态联动校验。                                                                                                                                                   |
| loveqq-boot-starter-discovery-nacos/loveqq-boot-starter-config-nacos | 集成了 nacos 服务发现、服务配置                                                                                                                                                                                  |
| loveqq-boot-starter-dubbo                                            | 集成了 dubbo 自动配置                                                                                                                                                                                       |
| loveqq-boot-starter-feign                                            | 集成了 feign 自动配置，可配合注册中心使用，支持 loveqq-mvc 注解                                                                                                                                                            |
| loveqq-boot-starter-spring-tx                                        | 集成了 spring-tx 模块实现事务集成(排除了 spring core 等多余模块)                                                                                                                                                        |
| loveqq-boot-starter-rocketmq                                         | 集成了 rocketmq 自动配置                                                                                                                                                                                    |
| loveqq-boot-starter-redisson                                         | 集成了基于 redisson 的 redis 支持，同时内置了基于 redis 的简易 mq 实现                                                                                                                                                    |
| loveqq-boot-starter-quartz                                           | 集成了 quartz 实现定时任务                                                                                                                                                                                    |
| loveqq-boot-starter-xxl-job                                          | 集成了 xxl-job 实现定时任务                                                                                                                                                                                   |
| loveqq-boot-starter-mybatis                                          | 集成了 mybatis 自动配置                                                                                                                                                                                     |
| mybatis-flex-loveqq-starter                                          | 集成了 mybatis-flex 自动配置，详情请查看 mybatis-flex 仓库                                                                                                                                                          |
| sa-token-loveqq-boot-starter                                         | 集成了 sa-token 自动配置，详情请查看 sa-token 仓库                                                                                                                                                                  |
| loveqq-boot-starter-shiro                                            | 集成了 shiro 自动配置                                                                                                                                                                                       |
| loveqq-boot-starter-mail                                             | 集成了 java mail 自动配置                                                                                                                                                                                   |
| loveqq-boot-starter-logback                                          | 集成了 logback 自动配置                                                                                                                                                                                     |
| loveqq-boot-starter-test                                             | 框架单元测试，同时引入该模块即可使用配置文件的 IDEA 自动提示                                                                                                                                                                    |
| loveqq-sdk                                                           | 用于快速封装 sdk，只需编写请求及响应模型，支持拦截器，易于拓展，支持命令式、响应式风格                                                                                                                                                        |
| loveqq-javafx                                                        | javafx mvvm 框架，实现了视图和数据模型的双向绑定。代码中只需操作数据即可反应到视图上，视图上编辑数据即可反应到模型里。<br/>详情请点击 [loveqq-javafx 模块 README.md.](./loveqq-javafx/README.md)                                                                 |
| Ruoyi-for-loveqq                                                     | 基于 loveqq 框架的单体版若依，去除了底层 spring 及其 spring boot starter。详情请查看仓库列表。                                                                                                                                    |


### 代码示例
```java
package com.kfyty.demo;

// import 省略

/**
 * 添加 @EnableWebMvc 以自动配置 web server
 * 添加 @RestController 标记是一个 mvc 控制器
 * 添加 @RequestMapping(expose = true)，设置 mvc 控制的统一路径前缀，expose = true 表示自动暴露该类的公开方法为 POST 路由
 * 添加 @BootApplication 以自动配置框架
 */
@Slf4j
@EnableWebMvc
@RestController
@BootApplication
@RequestMapping(expose = true)
public class Main implements ApplicationListener<ContextRefreshedEvent> {
    /**
     * 简单的配置注入
     */
    @Value("${k.name:test}")
    private String name;

    /**
     * 复杂的配置注入，此时 value 表示配置前缀
     * 可代替 {@link ConfigurationProperties}
     */
    @Value(value = "k.config.mains", bind = true)
    private Map<String, List<Main>> mains;

    /**
     * 实现 ApplicationListener 接口以实现事件监听
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("started succeed !");
    }

    /**
     * main 方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        /**
         * 设置全局懒加载，也可在配置文件配置
         */
        System.setProperty(ConstantConfig.LAZY_INIT_KEY, Boolean.TRUE.toString());

        /**
         * 设置多线程并发实例化 bean，也可在配置文件配置
         */
        System.setProperty(ConstantConfig.CONCURRENT_INIT_KEY, Boolean.TRUE.toString());

        /**
         * 运行
         */
        K.run(Main.class, args);
    }

    /**
     * 任意 bean 都可以通过该方式进行自定义
     */
    @Bean
    public BeanCustomizer<Main> mainBeanCustomizer() {
        return System.out::println;
    }

    /**
     * 添加 @Cacheable 将结果进行缓存
     * 公开方法，自动暴露为 POST 路由，默认路径地址是方法名称
     */
    @Cacheable
    public User hello(@Valid User user) {
        return user;
    }

    /**
     * 添加 @Cacheable 将结果进行缓存，响应式结果也是一样的写法！
     * 添加 @GetMapping 标记是一个 mvc GET 路由，默认路径地址是方法名称，免去复制方法名称的步骤
     */
    @Cacheable
    @GetMapping
    public Mono<User> helloAsync(@Valid User user) {
        return Mono.just(user);
    }

    @Data
    public static class User {
        /**
         * 添加 @Condition 以 ognl 表达式校验字段
         */
        @Condition(when = "type == 1", then = "photo != null", message = "type=1时，图片不能为空")
        private Integer type;

        private String photo;
    }

    /**
     * 添加 @This，标记该 bean 应该始终从代理对象调用
     * 添加 @Async 以支持异步调用
     * 添加 @EventListener 以支持注解事件监听
     */
    @This
    @Async
    @Service
    @EventListener
    public static class CustomizeEventListener {
        /**
         * 添加 @Async 该方法将被异步调用
         * 添加 @EventListener 标记是一个事件监听方法
         */
        @Async
        @EventListener
        public void on(PropertyConfigRefreshedEvent event) {
            System.out.println(event);
        }

        /**
         * 添加 @TransactionalEventListener 标记该监听器应该在事务内执行
         */
        @TransactionalEventListener
        public void onCustomize(PropertyConfigRefreshedEvent event) {
            // 由于该 bean 标记了 @This，因此即使 this 调用，也将是一个异步调用
            this.on(event);
        }

        /**
         * 添加 @Async.Await 标记从该方法内调用异步方法时，应自动同步
         * 添加 @Transactional 标记声明式事务
         */
        @Async.Await
        @Transactional
        public void save() {
            // 本来说，由于该 bean 标记了 @This，因此即使 this 调用，也将是一个异步调用
            // 但是，由于该方法被 @Async.Await 标记，因此该调用将自动变成一个同步调用
            this.on(null);
        }
    }

    /**
     * 添加 @Lazy 标记应该懒加载
     * 添加 @RefreshScope 标记该 bean 的作用域是 refresh，当配置变更时应该重建
     * 实现 InitializingBean/DestroyBean 以实现初始化和销毁
     */
    @Lazy
    @Component
    @RefreshScope
    public static class LifecycleBean implements InitializingBean, DestroyBean, BeanPostProcessor, BeanFactoryPreProcessor, BeanFactoryPostProcessor {
        /**
         * 实现 BeanFactoryPreProcessor 以配置 BeanFactory，实现更高级的功能
         * 该接口生命周期更靠前，适合更早的处理，eg：日志的自动配置
         *
         * @param beanFactory bean 工厂
         */
        @Override
        public void preProcessBeanFactory(BeanFactory beanFactory) {

        }

        /**
         * 实现 BeanFactoryPostProcessor 以修改 BeanFactory，实现更高级的功能
         * 此时 bean 定义已加载
         *
         * @param beanFactory bean 工厂
         */
        @Override
        public void postProcessBeanFactory(BeanFactory beanFactory) {

        }

        /**
         * 实现 BeanPostProcessor 以实现更精确的控制
         */
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            return null;
        }

        @Override
        public void afterPropertiesSet() {

        }

        @Override
        public void destroy() {

        }
    }

    /**
     * 下面演示构造器循环依赖
     * 这种情况不建议，但是框架提供解决方案
     * 解决方案有两种，任选一种即可
     * 1、在任意一个 bean 上添加 @Lazy 注解
     * 2、在任意一个 bean 的构造器方法上添加 @Lazy 注解
     */
    @Lazy                                                           // 方案1
    @Component
    public static class CycleBeanA {
        private final CycleBeanB cycleBeanB;

        public CycleBeanA(CycleBeanB cycleBeanB) {
            this.cycleBeanB = cycleBeanB;
        }
    }

    @Component
    public static class CycleBeanB {
        private final CycleBeanA cycleBeanA;

        public CycleBeanB(@Lazy CycleBeanA cycleBeanA) {            // 方案2
            this.cycleBeanA = cycleBeanA;
        }
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
    <version>1.1.5</version>
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
    implementation 'com.kfyty:loveqq-framework:1.1.3@pom'
    implementation 'com.kfyty:loveqq-boot:1.1.3'
    implementation 'com.kfyty:loveqq-boot-starter-logback:1.1.3'
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

### 接口文档
接口文档建议使用 smart-doc，仅需编写正常的 javadoc 注释即可，无法大量额外的注解，
因此框架内置了 smart-doc maven 插件，开发者仅需添加 smart-doc 配置即可。
配置应添加到项目的下述位置
```txt
./src/main/resources/smart-doc.json
```
参考配置文件示例
```json
{
  "isStrict": false,                                // 关闭严格模式，否则会强制检查代码注释
  "allInOne": true,                                 // 将文档合并到一个文件中
  "coverOld": true,                                 // 每次生成文档都覆盖旧文档
  "createDebugPage": true,                          // 创建 html debug 页面
  "outPath": "./src/main/resources/static/doc",     // 文档输出路径，建议使用该配置，则访问 /doc/debug-all.html 即可。若修改配置，则可能配置 mvc 映射路径
  "packageFilters": "com.demo.controller.*",        // 控制器包名扫描配置
  "requestFieldToUnderline": false,
  "responseFieldToUnderline": false
}
```
