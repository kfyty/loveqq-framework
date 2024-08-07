package com.kfyty.core;

import com.kfyty.loveqq.framework.core.utils.PropertiesUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/10/21 20:21
 * @email kfyty725@hotmail.com
 */
public class PropertiesTest {

    @Test
    public void propertiesTest() {
        Properties load = PropertiesUtil.load("application.properties");
        Assertions.assertEquals(load.getProperty("k.nested.prop"), "哈喽");
    }
}
