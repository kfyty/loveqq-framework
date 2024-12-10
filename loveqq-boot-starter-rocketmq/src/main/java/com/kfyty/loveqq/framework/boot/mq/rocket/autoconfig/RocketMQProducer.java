package com.kfyty.loveqq.framework.boot.mq.rocket.autoconfig;

import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.apis.producer.Transaction;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 描述: 生产者
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@Getter
@RequiredArgsConstructor
public class RocketMQProducer implements Closeable {
    /**
     * {@link ClientServiceProvider}
     */
    private final ClientServiceProvider provider;

    /**
     * 实际生产者
     */
    private final Producer producer;

    public Transaction beginTransaction() throws ClientException {
        return this.producer.beginTransaction();
    }

    public PreparedMessage prepare() {
        return new PreparedMessage(this.provider.newMessageBuilder());
    }

    @Override
    public void close() throws IOException {
        this.producer.close();
    }

    @RequiredArgsConstructor
    public class PreparedMessage {
        /**
         * 消息构建器
         */
        private final MessageBuilder builder;

        /**
         * 设置分组即表示顺序消息
         *
         * @param group 消息分组
         * @return this
         * @see https://rocketmq.apache.org/zh/docs/domainModel/02topic
         */
        public PreparedMessage setGroup(String group) {
            this.builder.setMessageGroup(group);
            return this;
        }

        public PreparedMessage setTopic(String topic) {
            this.builder.setTopic(topic);
            return this;
        }

        public PreparedMessage setTag(String tag) {
            this.builder.setTag(tag);
            return this;
        }

        public PreparedMessage setKeys(String... keys) {
            this.builder.setKeys(keys);
            return this;
        }

        public PreparedMessage setJSON(Object body) {
            if (body instanceof CharSequence) {
                return this.setBody(body.toString());
            }
            return this.setBody(JsonUtil.toJSONString(body));
        }

        public PreparedMessage setBody(String body) {
            return this.setBody(body, StandardCharsets.UTF_8);
        }

        public PreparedMessage setBody(String body, Charset charsets) {
            return this.setBody(body.getBytes(charsets));
        }

        public PreparedMessage setBody(byte[] body) {
            this.builder.setBody(body);
            return this;
        }

        public PreparedMessage addProperty(String key, String value) {
            this.builder.addProperty(key, value);
            return this;
        }

        public SendReceipt send() throws ClientException {
            return producer.send(this.builder.build());
        }

        public SendReceipt send(Transaction transaction) throws ClientException {
            return producer.send(this.builder.build(), transaction);
        }

        public SendReceipt sendTo(String topic) throws ClientException {
            return this.setTopic(topic).send();
        }

        public SendReceipt sendTo(String topic, Transaction transaction) throws ClientException {
            return this.setTopic(topic).send(transaction);
        }

        public SendReceipt sendTo(String group, String topic) throws ClientException {
            return this.setGroup(group).sendTo(topic);
        }

        public SendReceipt sendTo(String group, String topic, Transaction transaction) throws ClientException {
            return this.setGroup(group).sendTo(topic, transaction);
        }

        public SendReceipt sendDelay(long deliveryTimestamp) throws ClientException {
            this.builder.setDeliveryTimestamp(deliveryTimestamp);
            return this.send();
        }

        public SendReceipt sendDelay(long deliveryTimestamp, Transaction transaction) throws ClientException {
            this.builder.setDeliveryTimestamp(deliveryTimestamp);
            return this.send(transaction);
        }

        public SendReceipt sendDelayTo(String topic, long deliveryTimestamp) throws ClientException {
            return this.setTopic(topic).sendDelay(deliveryTimestamp);
        }

        public SendReceipt sendDelayTo(String topic, long deliveryTimestamp, Transaction transaction) throws ClientException {
            return this.setTopic(topic).sendDelay(deliveryTimestamp, transaction);
        }

        public SendReceipt sendDelayTo(String group, String topic, long deliveryTimestamp) throws ClientException {
            return this.setGroup(group).sendDelayTo(topic, deliveryTimestamp);
        }

        public SendReceipt sendDelayTo(String group, String topic, long deliveryTimestamp, Transaction transaction) throws ClientException {
            return this.setGroup(group).sendDelayTo(topic, deliveryTimestamp, transaction);
        }

        public CompletableFuture<SendReceipt> sendAsync() {
            return producer.sendAsync(this.builder.build());
        }

        public CompletableFuture<SendReceipt> sendAsyncTo(String topic) {
            return this.setTopic(topic).sendAsync();
        }

        public CompletableFuture<SendReceipt> sendAsyncTo(String group, String topic) {
            return this.setGroup(group).sendAsyncTo(topic);
        }

        public CompletableFuture<SendReceipt> sendDelayAsync(long deliveryTimestamp) {
            this.builder.setDeliveryTimestamp(deliveryTimestamp);
            return this.sendAsync();
        }

        public CompletableFuture<SendReceipt> sendDelayAsyncTo(String topic, long deliveryTimestamp) {
            return this.setTopic(topic).sendDelayAsync(deliveryTimestamp);
        }

        public CompletableFuture<SendReceipt> sendDelayAsyncTo(String group, String topic, long deliveryTimestamp) {
            return this.setGroup(group).sendDelayAsyncTo(topic, deliveryTimestamp);
        }
    }
}
