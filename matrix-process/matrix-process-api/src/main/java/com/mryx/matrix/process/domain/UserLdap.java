package com.mryx.matrix.process.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 来自LDAP的用户
 */
@Data
public class UserLdap extends BasePage implements Serializable{
    private static final long serialVersionUID = 6034484547086694254L;
    /*用户ID*/
    private Integer id;
    /*用户名*/
    private String userName;
    /*创建时间*/
    private Date createTime;
    /*更新时间*/
    private Date updateTime;
}