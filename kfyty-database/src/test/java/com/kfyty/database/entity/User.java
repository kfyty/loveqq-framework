package com.kfyty.database.entity;

import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

@Data
public class User implements Comparable<User> {
    private Integer id;
    private String username;
    private Date createTime;
    private byte[] image;

    public static User create() {
        User user = new User();
        user.setId(new Random().nextInt(9999));
        user.setUsername("test");
        user.setCreateTime(new Date());
        user.setImage("hello".getBytes(StandardCharsets.UTF_8));
        return user;
    }

    @Override
    public int compareTo(User o) {
        return 0;
    }
}
