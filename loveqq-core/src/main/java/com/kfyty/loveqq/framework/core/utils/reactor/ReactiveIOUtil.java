package com.kfyty.loveqq.framework.core.utils.reactor;

import com.kfyty.loveqq.framework.core.utils.IOUtil;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.kfyty.loveqq.framework.core.support.DefaultCompleteConsumer.DEFAULT_COMPLETE_CONSUMER;

/**
 * 描述: 响应式 io 工具
 *
 * @author kfyty725
 * @date 2023/11/15 17:50
 * @email kfyty725@hotmail.com
 */
public abstract class ReactiveIOUtil {
    /**
     * 默认的 HttpClient
     */
    private static HttpClient DEFAULT_CLIENT;

    static {
        DEFAULT_CLIENT = HttpClient.newBuilder().build();
    }

    /**
     * 获取 http 客户端
     *
     * @return http 客户端
     */
    public static HttpClient configure() {
        return DEFAULT_CLIENT;
    }

    /**
     * 设置 http 客户端
     *
     * @param httpClient http 客户端
     */
    public static void configure(HttpClient httpClient) {
        DEFAULT_CLIENT = httpClient;
    }

    /**
     * 下载到指定目录
     *
     * @param url     url
     * @param dirName 目录
     * @return 文件
     */
    public static Mono<File> downloadAsync(String url, String dirName) {
        String extension = IOUtil.getFileExtension(url);
        String randomName = UUID.randomUUID().toString().replace("-", "");
        return downloadAsync(url, dirName, extension == null ? randomName : randomName + '.' + extension);
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
                .map(e -> new File(e, fileName).toPath())
                .flatMap(e -> Mono.fromCompletionStage(downloadAsync(URI.create(url.replace(" ", "%20")), e)));
    }

    /**
     * 下载到指定目录
     *
     * @param url  url
     * @param path 文件目录
     * @return 文件
     */
    public static CompletableFuture<File> downloadAsync(URI url, Path path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .build();
        return DEFAULT_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofFile(path))
                .thenApplyAsync(e -> e.body().toFile())
                .whenComplete(DEFAULT_COMPLETE_CONSUMER);
    }
}
