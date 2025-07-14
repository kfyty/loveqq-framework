package com.kfyty.loveqq.framework.sdk.api.core.reactive.test;

import com.kfyty.loveqq.framework.sdk.api.core.config.ApiConfiguration;
import com.kfyty.loveqq.framework.sdk.api.core.reactive.http.ReactiveApis;
import com.kfyty.loveqq.framework.sdk.api.core.reactive.http.executor.ReactiveHttpClientHttpRequestExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HttpTest {

    @BeforeAll
    public static void set() {
        ApiConfiguration
                .getGlobalConfiguration()
                .setRequestExecutor(ReactiveHttpClientHttpRequestExecutor.INSTANCE);
    }

    @Test
    public void test() {
        String body = new ReactiveApis()
                .requestPath("https://www.baidu.com/s")
                .addQuery("wd", "hello")
                .contentType("text")
                .executeAsync()
                .map(String::new)
                .block();
        System.out.println(body);
    }
}
