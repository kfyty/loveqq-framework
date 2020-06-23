package com.kfyty.database.test.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Integer id;
    private String username;
    private Date createTime;
}
