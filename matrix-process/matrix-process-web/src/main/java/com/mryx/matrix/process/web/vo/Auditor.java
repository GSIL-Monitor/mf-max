package com.mryx.matrix.process.web.vo;

import java.io.Serializable;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-02 17:12
 **/
public class Auditor implements Serializable {

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 显示名称
     */
    private String nickName;

    /**
     * 电话
     */
    private String telephone;

    /**
     * 邮箱地址
     */
    private String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Auditor [" +
                "userName='" + userName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ']';
    }
}
