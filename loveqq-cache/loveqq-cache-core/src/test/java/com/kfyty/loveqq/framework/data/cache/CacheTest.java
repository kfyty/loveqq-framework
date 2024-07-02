package com.kfyty.loveqq.framework.data.cache;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.DefaultCache;
import org.junit.Assert;
import org.junit.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/4 10:44
 * @email kfyty725@hotmail.com
 */
public class CacheTest {

    @Test
    public void cacheTest() {
        Cache cache = new DefaultCache();
        cache.put("test", 1);
        cache.put("test", 2, 2);
        CommonUtil.sleep(3000);
        Assert.assertNull(cache.get("test"));
    }
}
