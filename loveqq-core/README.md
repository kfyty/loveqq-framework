## loveqq-core

### 核心工具
* 通用工具类：com.kfyty.loveqq.framework.core.utils.CommonUtil
* 包扫描工具类：com.kfyty.loveqq.framework.core.utils.PackageUtil
* 反射工具类：com.kfyty.loveqq.framework.core.utils.ReflectUtil
* 注解工具类：com.kfyty.loveqq.framework.core.utils.AnnotationUtil
   * 通过该工具类获取注解，才支持 @AliasFor 注解加持的注解继承、注解别名
* JSON 工具类：com.kfyty.loveqq.framework.core.utils.JsonUtil
* 配置文件属性工具类：com.kfyty.loveqq.framework.core.utils.PropertiesUtil
  * 支持 k.config.include 包含其他配置文件
  * 支持 .properties .yaml 格式配置文件
* 占位符解析工具类：com.kfyty.loveqq.framework.core.utils.PlaceholdersUtil
  * 支持 ${a.b} 占位符解析
  * 支持 ${ref:a.b} 直接引用整个 a.b 下的整个配置
* 简单数据类型转换工具类：com.kfyty.loveqq.framework.core.utils.ConverterUtil
* IO 工具类：com.kfyty.loveqq.framework.core.utils.IOUtil
* jdbc 工具类：com.kfyty.loveqq.framework.core.utils.JdbcUtil
* 数据库 ResultSet 工具类：com.kfyty.loveqq.framework.core.utils.ResultSetUtil
* 多线程执行管理工具类：com.kfyty.loveqq.framework.core.utils.CompletableFutureUtil
* ognl 工具类：com.kfyty.loveqq.framework.core.utils.OgnlUtil
* AOP 工具类：com.kfyty.loveqq.framework.core.utils.AopUtil
* 序列化工具类：com.kfyty.loveqq.framework.core.utils.SerializableUtil
* lambda 序列化工具类：com.kfyty.loveqq.framework.core.utils.SerializableLambdaUtil

### 扩展支持
* 泛型解析支持：com.kfyty.loveqq.framework.core.generic.SimpleGeneric
* 单线程任务支持：com.kfyty.loveqq.framework.core.thread.SingleThreadTask
  * 适用于单线程监控多个资源时使用
* 弱引用线程安全的 Map：com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap
* 限流器：com.kfyty.loveqq.framework.core.lang.util.concurrent.RunningLimiter
* 数据映射工具：com.kfyty.loveqq.framework.core.lang.util.Mapping
* 懒加载支持：com.kfyty.loveqq.framework.core.lang.Lazy
* 单值包装：com.kfyty.loveqq.framework.core.lang.Value
* 双值包装：com.kfyty.loveqq.framework.core.support.Pair
* 三值包装：com.kfyty.loveqq.framework.core.support.Triple
* 事务支持：com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction
* 文件监听器：com.kfyty.loveqq.framework.core.support.io.FileListener
* ant 路径匹配器：com.kfyty.loveqq.framework.core.support.PatternMatcher
  * 默认实现：com.kfyty.loveqq.framework.core.support.AntPathMatcher
* 可命名的线程工厂：com.kfyty.loveqq.framework.core.thread.NamedThreadFactory
* 接口默认方法执行器：com.kfyty.loveqq.framework.core.reflect.DefaultMethodInvoker
* SPI 加载器：com.kfyty.loveqq.framework.core.io.FactoriesLoader
  * 默认加载 META-INF/k.factories 配置内的配置
* 注解继承/注解别名支持：com.kfyty.loveqq.framework.core.lang.annotation.AliasFor
  * 如果需要递归查询父类注解，可实现：com.kfyty.loveqq.framework.core.lang.annotation.Inherited
* 动态代理工厂：com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory
  * jdk 实现：com.kfyty.loveqq.framework.core.proxy.factory.JdkDynamicProxyFactory
  * javassist 实现：com.kfyty.loveqq.framework.core.proxy.factory.JavassistDynamicProxyFactory
  * 代理拦截链：com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain
  * aop 联盟接口适配器：com.kfyty.loveqq.framework.core.proxy.aop.adapter.MethodProxyInvocationAdapter

### 自动配置接口
#### loveqq-framework 所有的自动配置相关接口都在这里，具体实现在 loveqq-boot，开发启动器时，强烈建议只能依赖 loveqq-core 的接口.
* 框架常量定义：com.kfyty.loveqq.framework.core.lang.ConstantConfig
* bean 定义：com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition
* bean 工厂：com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory
* 应用上下文接口：com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext
* bean 工厂前置处理器：com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor
* bean 工厂后置处理器：com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPostProcessor
* 工厂 bean：com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean
* bean 后置处理器：com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor
* 初始化 bean 接口：com.kfyty.loveqq.framework.core.autoconfig.InitializingBean
* 销毁 bean 接口：com.kfyty.loveqq.framework.core.autoconfig.DestroyBean
* 条件 bean 匹配接口：com.kfyty.loveqq.framework.core.autoconfig.condition.Condition
* 配置属性上下文：com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext
* 数据绑定器：com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder
* 组件匹配器：com.kfyty.loveqq.framework.core.autoconfig.beans.filter.ComponentMatcher
* 自动注入能力：com.kfyty.loveqq.framework.core.autoconfig.beans.AutowiredCapableSupport
* 自动注入元数据描述解析器：com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescriptionResolver
* 数据类型转换器：com.kfyty.loveqq.framework.core.converter.Converter
* 事件监听器：com.kfyty.loveqq.framework.core.event.ApplicationListener
* 事件适配器：com.kfyty.loveqq.framework.core.event.EventListenerAdapter
* 事件发布器：com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher
* 国际化接口：com.kfyty.loveqq.framework.core.i18n.I18nResourceBundle
* 作用域代理工厂：com.kfyty.loveqq.framework.core.autoconfig.scope.ScopeProxyFactory
* 更多请查看包：com.kfyty.loveqq.framework.core.autoconfig.** 下的文件