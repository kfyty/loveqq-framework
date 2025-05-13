package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 事件字节
     */
    public static final byte[] EVENT_BYTES = "event:".getBytes(UTF_8);

    /**
     * id 字节
     */
    public static final byte[] ID_BYTES = "id:".getBytes(UTF_8);

    /**
     * 重试字节
     */
    public static final byte[] RETRY_BYTES = "retry:".getBytes(UTF_8);

    /**
     * 数据字节
     */
    public static final byte[] DATA_BYTES = "data:".getBytes(UTF_8);

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
        Mapping.from(this.event).whenNotNull(e -> buffer.writeBytes(EVENT_BYTES).writeBytes(e.getBytes(UTF_8)).writeBytes(LINE_SEPARATOR));
        Mapping.from(this.id).whenNotNull(e -> buffer.writeBytes(ID_BYTES).writeBytes(e.getBytes(UTF_8)).writeBytes(LINE_SEPARATOR));
        Mapping.from(this.retry).whenNotNull(e -> buffer.writeBytes(RETRY_BYTES).writeBytes(String.valueOf(e).getBytes(UTF_8)).writeBytes(LINE_SEPARATOR));
        if (this.data instanceof Number || this.data instanceof Boolean || this.data instanceof CharSequence) {
            return buffer.writeBytes(DATA_BYTES).writeBytes(this.data.toString().getBytes(UTF_8)).writeBytes(MESSAGE_SEPARATOR);
        }
        if (this.data instanceof byte[]) {
            return buffer.writeBytes(DATA_BYTES).writeBytes((byte[]) this.data).writeBytes(MESSAGE_SEPARATOR);
        }
        throw new IllegalArgumentException("The sse data must be Number/String/byte[]");
    }
}
