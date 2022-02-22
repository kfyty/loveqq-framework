package com.kfyty.sdk.api.core.api;

import com.kfyty.sdk.api.core.AbstractApi;
import com.kfyty.sdk.api.core.ApiResponse;
import com.kfyty.sdk.api.core.annotation.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/2/22 20:39
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BaiduSearchApi extends AbstractApi<BaiduSearchApi, ApiResponse> {
    @Parameter("wd")
    private String word;

    @Override
    public String requestURL() {
        return "https://www.baidu.com/s";
    }
}
