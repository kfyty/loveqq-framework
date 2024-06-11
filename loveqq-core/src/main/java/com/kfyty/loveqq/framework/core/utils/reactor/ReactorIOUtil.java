package com.kfyty.loveqq.framework.core.utils.reactor;

import com.kfyty.loveqq.framework.core.utils.IOUtil;
import reactor.core.publisher.Mono;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static com.kfyty.loveqq.framework.core.support.DefaultCompleteConsumer.DEFAULT_COMPLETE_CONSUMER;
import static java.net.http.HttpResponse.BodyHandlers.ofByteArray;

/**
 * 描述: 响应式 io 工具
 *
 * @author kfyty725
 * @date 2023/11/15 17:50
 * @email kfyty725@hotmail.com
 */
public abstract class ReactorIOUtil {
    /**
     * 默认的 HttpClient
     */
    private static final HttpClient DEFAULT_CLIENT = HttpClient.newBuilder().build();

    /**
     * 下载到指定目录
     *
     * @param url     url
     * @param dirName 目录
     * @return 文件
     */
    public static Mono<File> downloadAsync(String url, String dirName) {
        return downloadAsync(url, dirName, UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * 下载到指定目录
     *
     * @param url      url
     * @param dirName  目录
     * @param fileName 文件名称
     * @return 文件
     */
    public static Mono<File> downloadAsync(String url, String dirName, String fileName) {
        return Mono.just(dirName)
                .doOnNext(IOUtil::ensureFolderExists)
                .map(e -> new File(e + "/" + fileName))
                .zipWith(Mono.fromSupplier(() -> URI.create(url)))
                .map(e -> e.mapT2(uri -> HttpRequest.newBuilder().uri(uri).build()))
                .map(e -> e.mapT2(request -> Mono.fromCompletionStage(DEFAULT_CLIENT.sendAsync(request, ofByteArray()).whenComplete(DEFAULT_COMPLETE_CONSUMER))))
                .flatMap(tuple -> tuple.getT2()
                        .map(HttpResponse::body)
                        .flatMap(body -> {
                            try (BufferedOutputStream bos = new BufferedOutputStream(IOUtil.newOutputStream(tuple.getT1()))) {
                                IOUtil.copy(new ByteArrayInputStream(body), bos);
                                return Mono.just(tuple.getT1());
                            } catch (IOException ex) {
                                return Mono.error(ex);
                            }
                        }));
    }
}
