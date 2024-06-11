package com.kfyty.database.vo;

import com.kfyty.database.entity.User;
import lombok.Data;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 14:12
 * @email kfyty725@hotmail.com
 */
@Data
public class UserVo implements Comparable<UserVo> {
    private Integer id;
    private User user;

    public static UserVo create(Integer id) {
        UserVo vo = new UserVo();
        vo.setId(id);
        return vo;
    }

    @Override
    public int compareTo(UserVo o) {
        return 0;
    }
}
