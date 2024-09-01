package com.kfyty.core;

import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/8/14 22:08
 * @email kfyty725@hotmail.com
 */
public class MappingTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class User {
        private Integer id;
        private String name;
    }

    @Test
    public void test() {
        User user = new User(1, "name");
        User copy = new User();
        Mapping.from(user).whenNotNull(e -> copy.setId(e.getId()));
        Assertions.assertEquals(user.getId(), copy.getId());

        Mapping.from("").whenNotEmpty(copy::setName);
        Assertions.assertNull(copy.getName());

        Mapping.from(user.getName()).whenNotEmpty(copy::setName);
        Assertions.assertEquals(copy.getName(), user.getName());

        Mapping.from("123").notNullMap(Integer::parseInt).whenNotNull(copy::setId);
        Assertions.assertEquals(Integer.valueOf(123), copy.getId());

        Mapping.from("456").notNullFlatMap(e -> Mapping.from(Integer.parseInt(e))).whenNotNull(copy::setId);
        Assertions.assertEquals(Integer.valueOf(456), copy.getId());

        Mapping<Integer> map1 = Mapping.from("789").map(Integer::parseInt);
        Mapping<String> back1 = map1.back(String.class);
        Assertions.assertEquals("789", back1.get());

        Mapping<Integer> map2 = Mapping.from("789").flatMap(e -> Mapping.from(Integer.parseInt(e)));
        Mapping<String> back2 = map2.back(String.class);
        Assertions.assertEquals("789", back2.get());
    }

    @Test
    public void test2() {
        User non = null;
        Mapping.from(null).whenNotNull(e -> non.setId(1));
        Mapping.from(null).whenNotEmpty(e -> non.setId(1));

        Mapping.from(null).notNullMap(e -> non.getId());
        Mapping.from(null).notNullFlatMap(e -> Mapping.from(non.getId()));

        Mapping.from(null).notEmptyMap(e -> non.getId());
        Mapping.from(null).notEmptyFlatMap(e -> Mapping.from(non.getId()));
    }
}
