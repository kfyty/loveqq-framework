package com.kfyty.database.test.vo;

import com.kfyty.database.test.entity.User;
import lombok.Data;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 14:12
 * @email kfyty725@hotmail.com
 */
@Data
public class UserVo {
    private Integer id;
    private User user;

    public static UserVo create(Integer id) {
        UserVo vo = new UserVo();
        vo.setId(id);
        return vo;
    }
}
