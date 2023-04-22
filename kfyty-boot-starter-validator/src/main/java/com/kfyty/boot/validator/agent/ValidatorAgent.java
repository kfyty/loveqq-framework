package com.kfyty.boot.validator.agent;

import java.lang.instrument.Instrumentation;

/**
 * 描述: 校验器代理
 *
 * @author kfyty725
 * @date 2023/4/14 15:31
 * @email kfyty725@hotmail.com
 */
public class ValidatorAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) throws InstantiationException {
        instrumentation.addTransformer(new ValidatorValueContextInstrumentation());
    }
}
