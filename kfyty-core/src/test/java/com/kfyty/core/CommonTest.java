package com.kfyty.core;

import com.kfyty.core.utils.CommonUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2023/3/22 21:06
 * @email kfyty725@hotmail.com
 */
public class CommonTest {

    @Test
    public void removePrefixTest() {
        String s = "/api/test/api";
        String removePrefix = CommonUtil.removePrefix("/api", s);
        Assert.assertEquals(removePrefix, "/test/api");
    }
}
