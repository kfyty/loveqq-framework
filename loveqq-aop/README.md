## loveqq-aop
    该模块是 AOP 具体实现，整合了 aspectj

### 主要切面实现
* 基于 ant 路径的实现：com.kfyty.loveqq.framework.aop.support.pattern.AntPathPointcutAdvisor
* 基于注解的实现：com.kfyty.loveqq.framework.aop.support.annotated.AnnotationPointcutAdvisor
* 基于 @Aspect 注解的实现：com.kfyty.loveqq.framework.aop.support.DefaultPointcutAdvisor.DefaultPointcutAdvisor
  * 基于 @Aspect 注解的切面创建器：com.kfyty.loveqq.framework.aop.aspectj.creator.AdvisorCreator
  * 基于 @Aspect 注解的切入点实现：com.kfyty.loveqq.framework.aop.aspectj.AspectJExpressionPointcut

### aop 桥接及适配器
    loveqq 框架中的代理并不全是基于 aop 的，大多数是直接基于 loveqq-core 模块的代理工厂及代理链实现的。因此需要将 aop 和代理链整合，使 aop 成为代理链的一部分。
* aop 切面代理：com.kfyty.loveqq.framework.aop.proxy.AspectMethodInterceptorProxy
  * 由此代理开始，进入 aop 的代理
* aop 桥接代理：com.kfyty.loveqq.framework.aop.proxy.AopInterceptorChainBridgeProxy
  * 由此代理开始，返回原代理链继续执行

### 自动配置
* 切面处理器：com.kfyty.loveqq.framework.aop.processor.AspectJBeanPostProcessor
  * 由于性能的考虑，并不是所有的类都会应用该处理器，只有被 @AspectResolve 注解的类才会被解析
  * 默认情况下：
    * @Component **不会**被解析
    * @Service **会**被解析，因为一般是业务组件
    * @Controller **会**被解析，因为一般是业务组件
