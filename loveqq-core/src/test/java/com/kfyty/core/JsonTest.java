package com.kfyty.core;

import com.kfyty.loveqq.framework.core.utils.JsonUtil;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2023/4/1 20:16
 * @email kfyty725@hotmail.com
 */
public class JsonTest {
    private static final String json = "{\n" +
            "\t\"id\": \"1\",\n" +
            "\t\"child\": {\n" +
            "\t\t\"id\": \"2\"\n" +
            "\t},\n" +
            "\t\"childStr\": {\n" +
            "\t\t\"id\": \"3\",\n" +
            "\t\t\"arr\": [\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"id\": 10\n" +
            "\t\t\t},\n" +
            "\t\t\t{\n" +
            "\t\t\t\t\"id\": 12\n" +
            "\t\t\t}\n" +
            "\t\t],\n" +
            "\t\t\"ids\": [\n" +
            "\t\t\t1,\n" +
            "\t\t\t2,\n" +
            "\t\t\t3\n" +
            "\t\t]\n" +
            "\t},\n" +
            "\t\"list\": [\n" +
            "\t\t{\n" +
            "\t\t\t\"id\": \"5\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"id\": \"6\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}";

    @Test
    public void test() {
        User user = JsonUtil.toObject(json, User.class);
        Assertions.assertEquals(user.getChildStr(), "{\"id\":\"3\",\"arr\":[{\"id\":10},{\"id\":12}],\"ids\":[1,2,3]}");
        Assertions.assertEquals(user.getList(), "[{\"id\":\"5\"},{\"id\":\"6\"}]");
    }

    @Data
    private static class User {
        private Long id;
        private User child;
        private String childStr;
        private String list;
    }
}
