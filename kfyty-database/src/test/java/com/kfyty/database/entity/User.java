package com.kfyty.database.entity;

import com.kfyty.database.jdbc.annotation.TableId;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Data
public class User implements Comparable<User> {
    @TableId
    private Integer id;
    private String username;
    private Date createTime;
    private byte[] image;

    public static User create() {
        User user = new User();
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
