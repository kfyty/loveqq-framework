package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 描述: sse
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseEventStream {
    /**
     * 消息行分隔符
     */
    public static final byte[] LINE_SEPARATOR = "\n".getBytes(UTF_8);

    /**
     * 消息行分隔符
     */
    public static final byte[] MESSAGE_SEPARATOR = "\n\n".getBytes(UTF_8);

    /**
     * 事件名称
     */
    private String event;

    /**
     * 事件 id
     */
    private String id;

    /**
     * 客户端自动重试，单位毫秒
     */
    private Integer retry;

    /**
     * 数据
     */
    private Object data;

    public ByteBuf build() {
        ByteBuf buffer = Unpooled.buffer();
        Mapping.from(this.event).whenNotNull(e -> buffer.writeBytes("event:".getBytes(UTF_8)).writeBytes(e.getBytes(UTF_8)).writeBytes(LINE_SEPARATOR));
        Mapping.from(this.id).whenNotNull(e -> buffer.writeBytes("id:".getBytes(UTF_8)).writeBytes(e.getBytes(UTF_8)).writeBytes(LINE_SEPARATOR));
        Mapping.from(this.retry).whenNotNull(e -> buffer.writeBytes("retry:".getBytes(UTF_8)).writeBytes(String.valueOf(e).getBytes(UTF_8)).writeBytes(LINE_SEPARATOR));
        if (this.data instanceof CharSequence) {
            return buffer.writeBytes("data:".getBytes(UTF_8)).writeBytes(this.data.toString().getBytes(UTF_8)).writeBytes(MESSAGE_SEPARATOR);
        }
        if (this.data instanceof byte[]) {
            return buffer.writeBytes("data:".getBytes(UTF_8)).writeBytes((byte[]) this.data).writeBytes(MESSAGE_SEPARATOR);
        }
        throw new IllegalArgumentException("The sse data must be String/byte[]");
    }
}
