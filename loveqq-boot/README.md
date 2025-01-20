## loveqq-boot
    该模块是自动配置的具体实现

### 主要默认实现
* 启动入口：
  * ide 中启动：
    * com.kfyty.loveqq.framework.boot.K.run(java.lang.Class<?>, java.lang.String...)
      * 该方法会自动构建 JarIndex 进行启动，会使用自定义的 JarIndexClassLoader 启动应用
    * com.kfyty.loveqq.framework.boot.K.start(java.lang.Class<?>, java.lang.String...)
      * 该方法会直接启动，使用默认的类加载器
  * jar 包启动：
    * jar 包启动时，默认会由 com.kfyty.loveqq.framework.core.support.BootLauncher 进行引导启动，
      此时会读取构建的 jar.idx 构建 JarIndex 后，使用自定义的 JarIndexClassLoader 启动应用
* 默认 bean 工厂/应用上下文实现：com.kfyty.loveqq.framework.boot.context.DefaultConfigurableApplicationContext
* 默认属性配置上下文实现：com.kfyty.loveqq.framework.boot.context.env.DefaultGenericPropertiesContext
* 默认数据绑定器实现：com.kfyty.loveqq.framework.boot.context.env.DefaultDataBinder
* 默认事件发布器实现：com.kfyty.loveqq.framework.boot.event.DefaultApplicationEventPublisher
* 事件监听注解注册器：com.kfyty.loveqq.framework.boot.event.EventListenerRegistry
* 默认国际化实现：com.kfyty.loveqq.framework.boot.i18n.DefaultI18nResourceBundle

### 主要配置示例
```yaml
k:
  java:
    system:
      # java 内部资源不在 jar index 内，JarIndexClassLoader 可能获取不到，可通过该参数设置，英文分号分隔
      resources: res1;res2
  transformer:
    # 是否读取 ClassFileTransformer 实现并应用，使用 JarIndexClassLoader 启动应用有效
    # 开启此配置，会使用 javassist 修改字节码，会增加少量内存使用量，但可以实现不配置 Pre-Main 的情况下，使 javaagent 生效
    load: true
  dependency:
    check: true                             # 是否读取进行依赖检查，出现依赖冲突导致启动失败时，可打开
    load-jar-class-path: true               # ClassLoaderUtil#resolveClassPath(ClassLoader) 时，是否读取 jar 内的 Class-Path 属性
  annotation:
    depth: 99                               # AnnotationUtil 解析注解时，发生循环嵌套注解时的解析深度，避免死循环
  application:
    name: loveqq                            # 应用名称，注册中心可以使用
    lazy-init: false                        # 应用是否整体懒加载，true 时，仅加载 bean 定义，所有 bean 都不会进行初始化
    concurrent-init: false                  # 应用是否并行初始化，true 时将使用线程池初始化所有的单例 bean
  server:
    port: 8080                              # web 服务器端口的配置
  config:
    include: config1.yml, config2.yml       # 包含其他配置
    location: nativeConfig.yml              # 本地磁盘的配置文件，通过该配置指定的配置文件，支持热刷新，注意：只有通过该配置指定的才支持热刷新，内部通过 include 包含的嵌套配置不支持
    load-system-property: false             # 是否加载系统属性到配置上下文
```