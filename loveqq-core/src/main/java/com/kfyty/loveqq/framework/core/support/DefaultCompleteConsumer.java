package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

/**
 * 描述: 默认的异步完成处理器
 *
 * @author kfyty725
 * @date 2023/11/15 17:50
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class DefaultCompleteConsumer implements BiConsumer<Object, Throwable> {
    public static final DefaultCompleteConsumer DEFAULT_COMPLETE_CONSUMER = new DefaultCompleteConsumer();

    @Override
    public void accept(Object unused, Throwable throwable) {
        if (throwable != null) {
            ResolvableException e = ExceptionUtil.wrap(throwable);
            log.error("DefaultCompleteConsumer: {}", e.getMessage());
            throw e;
        }
    }
}
