package com.kfyty.sdk.api.core;

import com.kfyty.sdk.api.core.api.BaiduSearchApi;
import com.kfyty.sdk.api.core.config.ApiConfiguration;
import org.junit.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/2/22 20:38
 * @email kfyty725@hotmail.com
 */
public class SdkTest {

    @Test
    public void test() {
        ApiConfiguration
                .getGlobalConfiguration()
                .getParameterProviderRegistry()
                .registryParameterProvider(api -> "test", "word");
        byte[] bytes = new BaiduSearchApi().execute();
        System.out.println(new String(bytes));
    }
}
