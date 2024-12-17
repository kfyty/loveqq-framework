package com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig;

import com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig.annotation.MessageBody;
import com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig.annotation.RocketMQMessageListener;
import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import com.kfyty.loveqq.framework.core.utils.NIOUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.consumer.PushConsumerBuilder;
import org.apache.rocketmq.client.apis.message.MessageId;
import org.apache.rocketmq.client.apis.message.MessageView;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 描述: rocketmq 消息监听器 bean
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
@ConditionalOnProperty(value = "rocketmq.endpoints", matchIfNonNull = true)
public class RocketMQMessageListenerRegistry implements BeanPostProcessor, BeanFactoryAware, DestroyBean {
    /**
     * bean 工厂
     */
    private BeanFactory beanFactory;

    /**
     * {@link ClientConfiguration}
     */
    @Autowired
    private ClientConfiguration clientConfiguration;

    /**
     * {@link ClientServiceProvider}
     */
    @Autowired
    private ClientServiceProvider clientServiceProvider;

    /**
     * 消费者
     */
    private final List<PushConsumer> consumers;

    public RocketMQMessageListenerRegistry() {
        this.consumers = new LinkedList<>();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object target = AopUtil.getTarget(bean);
        RocketMQMessageListener annotation = AnnotationUtil.findAnnotation(target, RocketMQMessageListener.class);
        if (annotation == null) {
            if (bean instanceof MessageListener) {
                throw new ResolvableException("Register rocketmq message listener failed, the bean " + beanName + " should annotated with RocketMQMessageListener.");
            }
            return null;
        }

        // 注册 MessageListener bean
        if (bean instanceof MessageListener) {
            this.registerRocketMQMessageListener(target.getClass(), new Lazy<>(() -> this.beanFactory.getBean(beanName)), annotation);
        }

        // 注册方法级监听器
        for (Method method : ReflectUtil.getMethods(target.getClass())) {
            RocketMQMessageListener methodAnnotation = AnnotationUtil.findAnnotation(method, RocketMQMessageListener.class);
            if (methodAnnotation != null) {
                this.registerRocketMQMessageListener(new Lazy<>(() -> this.beanFactory.getBean(beanName)), method, methodAnnotation);
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        log.info("Start destroy RocketMQ consumers...");
        for (PushConsumer consumer : this.consumers) {
            IOUtil.close(consumer);
        }
        log.info("Destroy RocketMQ consumers succeed.");
    }

    protected void registerRocketMQMessageListener(Class<?> beanType, Lazy<MessageListener> listener, RocketMQMessageListener annotation) {
        try {
            this.consumers.add(this.buildConsumer(annotation).setMessageListener(mv -> listener.get().consume(mv)).build());
            log.info("Registry RocketMQ message listener: '{}/{}' -> {}", annotation.consumerGroup(), annotation.value(), beanType);
        } catch (ClientException e) {
            throw new ResolvableException("Register rocketmq message listener failed: " + listener.get(), e);
        }
    }

    protected void registerRocketMQMessageListener(Lazy<Object> bean, Method method, RocketMQMessageListener annotation) {
        try {
            this.consumers.add(this.buildConsumer(annotation).setMessageListener(new ReflectiveMessageListener(bean, method)).build());
            log.info("Registry RocketMQ message listener: '{}/{}' -> {}", annotation.consumerGroup(), annotation.value(), method);
        } catch (ClientException e) {
            throw new ResolvableException("Register rocketmq message listener failed: " + method, e);
        }
    }

    protected PushConsumerBuilder buildConsumer(RocketMQMessageListener annotation) {
        PushConsumerBuilder builder = this.clientServiceProvider.newPushConsumerBuilder()
                .setClientConfiguration(this.clientConfiguration)
                .setConsumerGroup(annotation.consumerGroup())
                .setSubscriptionExpressions(Collections.singletonMap(annotation.value(), new FilterExpression(annotation.filterExpress(), annotation.filterType())));
        Mapping.from(annotation.maxCacheMessageCount()).when(e -> e > -1, builder::setMaxCacheMessageCount);
        Mapping.from(annotation.maxCacheMessageSizeInBytes()).when(e -> e > -1, builder::setMaxCacheMessageSizeInBytes);
        Mapping.from(annotation.consumptionThreadCount()).when(e -> e > -1, builder::setConsumptionThreadCount);
        return builder;
    }

    @RequiredArgsConstructor
    public static class ReflectiveMessageListener implements MessageListener {
        /**
         * 监听器实例
         */
        private final Lazy<Object> bean;

        /**
         * 监听器方法
         */
        private final Method method;

        @Override
        public ConsumeResult consume(MessageView messageView) {
            try {
                Object[] parameters = this.bindParameters(messageView, method);
                ReflectUtil.invokeMethod(this.bean.get(), this.method, parameters);
                return ConsumeResult.SUCCESS;
            } catch (Throwable e) {
                log.error("consumer message failed: {}", messageView.getMessageId(), e);
                return ConsumeResult.FAILURE;
            }
        }

        protected Object[] bindParameters(MessageView mv, Method method) {
            Parameter[] parameters = method.getParameters();
            Object[] params = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (MessageView.class.isAssignableFrom(parameter.getType())) {
                    params[i] = mv;
                    continue;
                }
                if (MessageId.class.isAssignableFrom(parameter.getType())) {
                    params[i] = mv.getMessageId();
                    continue;
                }
                if (ByteBuffer.class.isAssignableFrom(parameter.getType())) {
                    params[i] = mv.getBody();
                    continue;
                }
                if (parameter.getType() == byte[].class) {
                    params[i] = NIOUtil.read(mv.getBody());
                    continue;
                }
                if (CharSequence.class.isAssignableFrom(parameter.getType())) {
                    params[i] = new String(NIOUtil.read(mv.getBody()));
                    continue;
                }
                if (AnnotationUtil.hasAnnotation(parameter, MessageBody.class)) {
                    params[i] = JsonUtil.toObject(new String(NIOUtil.read(mv.getBody())), parameter.getParameterizedType());
                    continue;
                }
                if (ReflectUtil.isBaseDataType(parameter.getType())) {
                    params[i] = ConverterUtil.convert(new String(NIOUtil.read(mv.getBody())), parameter.getType());
                    continue;
                }
                throw new UnsupportedOperationException("Can't convert message type: " + parameter.getType());
            }
            return params;
        }
    }
}
