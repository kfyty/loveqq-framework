package com.kfyty.loveqq.framework.sdk.api.core.http;

import com.kfyty.loveqq.framework.sdk.api.core.ApiResponse;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 描述: 直接构建 api 支持
 *
 * @author kfyty725
 * @date 2021/11/11 14:10
 * @email kfyty725@hotmail.com
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Apis extends AbstractApi<Apis, ApiResponse> {
}
