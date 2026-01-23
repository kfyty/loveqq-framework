package com.kfyty.core;

import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.support.Triple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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
    public void filterTest() {
        Integer v1 = Mapping.<Integer>from(null).filter(e -> e.toString().equals("1")).get();
        Integer v2 = Mapping.from(1).filter(e -> e.toString().equals("1")).get();
        Integer v3 = Mapping.from(1).filter(e -> e.toString().equals("2")).get();
        Assertions.assertNull(v1);
        Assertions.assertEquals(v2, 1);
        Assertions.assertNull(v3);
    }

    @Test
    public void mapTest() {
        String v1 = Mapping.<Integer>from(null).map(Object::toString).get();
        String v2 = Mapping.from(1).map(e -> e == 1, Object::toString).get();
        String v3 = Mapping.from(1).map(e -> e != 1, Object::toString).get();
        Assertions.assertNull(v1);
        Assertions.assertEquals(v2, "1");
        Assertions.assertNull(v3);
    }

    @Test
    public void flatMapTest() {
        String v1 = Mapping.<Integer>from(null).flatMap(e -> Mapping.from(e.toString())).get();
        String v2 = Mapping.from(1).flatMap(e -> e == 1, e -> Mapping.from(e.toString())).get();
        String v3 = Mapping.from(1).flatMap(e -> e != 1, e -> Mapping.from(e.toString())).get();
        Assertions.assertNull(v1);
        Assertions.assertEquals(v2, "1");
        Assertions.assertNull(v3);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void thenTest() {
        boolean error = false;
        User user = new User();
        Mapping.from(null).then(Object::toString);
        Mapping.from(user).then(e -> e.setId(1));
        try {
            Mapping.from(1).then(e -> System.out.println(1 / 0), new Triple<>(Exception.class, ex -> user.setName(ex.getMessage()), RuntimeException::new));
        } catch (RuntimeException e) {
            error = true;
        }

        Assertions.assertEquals(user.getId(), 1);
        Assertions.assertNotNull(user.getName());
        Assertions.assertTrue(error);
    }

    @Test
    public void buildTest() {
        Mapping<Integer> v1 = Mapping.build(Optional.of(1));
        Mapping<Integer> v2 = Mapping.build(Mapping.from(1));
    }
}
