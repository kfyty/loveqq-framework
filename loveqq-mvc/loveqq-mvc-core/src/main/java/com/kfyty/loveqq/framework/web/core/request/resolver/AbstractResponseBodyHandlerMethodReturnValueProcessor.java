package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.support.AcceptRange;
import com.kfyty.loveqq.framework.web.core.request.support.RandomAccessStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述: {@link com.kfyty.loveqq.framework.web.core.annotation.bind.ResponseBody} 返回值处理器
 *
 * @author kfyty725
 * @date 2021/6/10 11:15
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractResponseBodyHandlerMethodReturnValueProcessor implements HandlerMethodReturnValueProcessor {
    /**
     * 请求的范围属性
     */
    public static final String MULTIPART_BYTE_RANGES_ATTRIBUTE = AbstractResponseBodyHandlerMethodReturnValueProcessor.class.getName() + "__multipart-byte-ranges__";

    /**
     * 多范围返回分隔符
     */
    public static final String MULTIPART_BOUNDARY = "LOVEQQ_MULTIPART_BYTE_RANGES";

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        if (returnType == null) {
            return false;
        }
        String contentType = ((MethodMapping) returnType.getMetadata()).getProduces();
        if (contentType == null) {
            return false;
        }
        boolean isResponseBody = hasAnnotation(returnType.getMethod(), ResponseBody.class) || hasAnnotation(returnType.getMethod().getDeclaringClass(), ResponseBody.class);
        return isResponseBody && this.supportsContentType(contentType);
    }

    protected abstract boolean supportsContentType(String contentType);

    /**
     * 准备写出随机读写流
     *
     * @param request  请求
     * @param response 响应
     * @param stream   流
     * @return 范围
     */
    public static List<AcceptRange> prepareRandomAccessStream(ServerRequest request, ServerResponse response, RandomAccessStream stream) {
        // 解析请求范围
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Last-Modified", String.valueOf(stream.lastModified()));
        List<AcceptRange> ranges = AcceptRange.resolve(request.getHeader("Range"), stream.length());

        // 没有范围时，设置 200，返回全部数据
        if (ranges.isEmpty() || stream instanceof RandomAccessStream.InputStreamRandomAccessAdapter) {
            response.setStatus(200);
            response.setContentType(stream.contentType());
            response.setHeader("Content-Length", String.valueOf(stream.length()));
            return ranges;
        }

        // 存在范围时，设置 206，返回请求的数据
        response.setStatus(206);

        // 只有一个范围，无需变更 content-type
        if (ranges.size() == 1) {
            AcceptRange range = ranges.get(0);
            response.setContentType(stream.contentType());
            response.setHeader("Content-Range", String.format("bytes %d-%d/%d", range.getPos(), range.getLast(), stream.length()));
            response.setHeader("Content-Length", String.valueOf(range.getLength()));
        }
        // 需要设置新的 content-type
        else {
            response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
            // 写数据在后续逻辑完成
        }
        return ranges;
    }

    /**
     * 处理输入流
     *
     * @param stream       输入流
     * @param ranges       解析的请求范围
     * @param byteConsumer 字节消费者，返回 false 将终止处理
     */
    public static void doHandleRandomAccessStreamReturnValue(RandomAccessStream stream, List<AcceptRange> ranges, BiFunction<Integer, byte[], Boolean> byteConsumer) throws IOException {
        // 此时写出全部数据，因此添加一个模拟范围数据即可
        if (ranges.isEmpty()) {
            ranges.add(new AcceptRange(0, stream.length() - 1));
        }

        // 开始处理数据
        boolean multipart = ranges.size() > 1;
        for (AcceptRange range : ranges) {
            // 初始化临时变量
            int read = 0;
            long total = 0L;
            byte[] bytes = new byte[Math.min(ConstantConfig.IO_STREAM_READ_BUFFER_SIZE << 4, (int) range.getLength())];

            // 多范围支持
            if (multipart) {
                String boundary = System.lineSeparator() +
                        "--" + MULTIPART_BOUNDARY +
                        "Content-Type: " + stream.contentType() +
                        "Content-Range: " + String.format("bytes %d-%d/%d", range.getPos(), range.getLast(), stream.length());
                byte[] boundaryBytes = boundary.getBytes(StandardCharsets.UTF_8);
                if (!byteConsumer.apply(boundaryBytes.length, boundaryBytes)) {
                    return;
                }
            }

            // 开始从指定位置读取
            stream.seed(range.getPos());
            while (read > -1 && total < range.getLength()) {
                read = stream.read(bytes, 0, (int) Math.min(bytes.length, range.getLength() - total));
                if (byteConsumer.apply(read, bytes)) {
                    total += read;
                    continue;
                }
                return;
            }
        }

        if (multipart) {
            String boundary = System.lineSeparator() + "--" + MULTIPART_BOUNDARY + "--";
            byte[] boundaryBytes = boundary.getBytes(StandardCharsets.UTF_8);
            byteConsumer.apply(boundaryBytes.length, boundaryBytes);
        }
    }
}
