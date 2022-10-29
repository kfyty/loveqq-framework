package com.kfyty.support;

import com.kfyty.core.utils.PropertiesUtil;
import org.junit.Test;

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
        System.out.println(load);
    }
}
