package com.mryx.matrix.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户信息dto
 *
 * @author wangwenbo
 * @create 2018-09-13 11:29
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto implements Serializable{


    private static final long serialVersionUID = 2480068182570991273L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 名字
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;
}
