package com.kfyty.loveqq.framework.boot.validator.test;

import com.kfyty.loveqq.framework.boot.validator.annotation.Group;
import com.kfyty.loveqq.framework.boot.validator.proxy.MethodValidationInterceptorProxy;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidTest {
    /**
     * 校验器
     */
    private Validator validator = Validation.byDefaultProvider()
            .configure()
            .buildValidatorFactory()
            .getValidator();

    /**
     * 测试类
     */
    private UserService userService = DynamicProxyFactory.create(true)
            .addInterceptorPoint(new MethodValidationInterceptorProxy(Lazy.of(() -> validator)))
            .createProxy(new UserService());

    /**
     * 直接校验参数，一般是基础数据类型
     */
    @Test
    public void parameterValidTest0() {
        this.userService.test("test");
        Assertions.assertThrows(ConstraintViolationException.class, () -> this.userService.test(null));
    }

    /**
     * 直接校验参数，一般是基础数据类型
     */
    @Test
    public void parameterValidTest1() {
        User user = new User();
        user.setAge(1);
        try {
            this.userService.test2(null, user);
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), ConstraintViolationException.class);
            Assertions.assertEquals(((ConstraintViolationException) e).getConstraintViolations().size(), 2);
        }

        try {
            this.userService.test2("test", user);
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), ConstraintViolationException.class);
            Assertions.assertEquals(((ConstraintViolationException) e).getConstraintViolations().size(), 1);
        }

        try {
            this.userService.test3(null, user);
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), ConstraintViolationException.class);
            Assertions.assertEquals(((ConstraintViolationException) e).getConstraintViolations().size(), 1);
        }

        try {
            user.setId(1L);
            this.userService.test3(null, user);
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), ConstraintViolationException.class);
            Assertions.assertEquals(((ConstraintViolationException) e).getConstraintViolations().size(), 1);
        }

        this.userService.test3("test", user);
    }

    /**
     * 校验对象
     * 校验默认分组
     */
    @Test
    public void parameterValidTest2() {
        try {
            User user = new User();
            this.userService.testUser(user);
            throw new IllegalStateException();
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), ConstraintViolationException.class);
            Assertions.assertEquals(((ConstraintViolationException) e).getConstraintViolations().size(), 2);
        }
    }

    /**
     * 校验对象
     * 校验 IdGroup
     */
    @Test
    public void parameterValidTest3() {
        User user = new User();

        try {
            this.userService.testUserId(user);
        } catch (Exception e) {
            Assertions.assertEquals(e.getClass(), ConstraintViolationException.class);
            Assertions.assertEquals(((ConstraintViolationException) e).getConstraintViolations().size(), 1);
        }

        user.setId(1L);
        this.userService.testUserId(user);
    }

    /**
     * 校验对象
     * 校验 IdGroup NameGroup
     */
    @Test
    public void parameterValidTest4() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setName("user2");

        this.userService.testUserIdAndName(user1, user2);
    }

    @Valid
    static class UserService {

        public void test(@NotBlank(message = "NotBlank") String test) {

        }

        public void test2(@NotBlank(message = "NotBlank") String test, @Valid User user) {

        }

        public void test3(@NotBlank(message = "NotBlank") String test, @Valid @Group(IdGroup.class) User user) {

        }

        public void testUser(@Valid User user) {

        }

        public void testUserId(@Valid @Group(IdGroup.class) User user) {

        }

        public void testUserIdAndName(@Valid @Group(IdGroup.class) User user1, @Valid @Group(NameGroup.class) User user2) {

        }
    }

    @Data
    static class User {
        @NotNull(message = "age")
        Integer age;

        @NotNull(message = "sex")
        Integer sex;

        @NotNull(message = "id", groups = IdGroup.class)
        Long id;

        @NotBlank(message = "name", groups = NameGroup.class)
        String name;
    }

    interface IdGroup {
    }

    interface NameGroup {
    }
}
