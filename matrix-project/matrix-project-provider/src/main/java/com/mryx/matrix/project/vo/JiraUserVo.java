package com.mryx.matrix.project.vo;

import com.mryx.matrix.project.domain.JiraUser;
import org.apache.commons.lang.StringUtils;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-11-14 14:26
 **/
public class JiraUserVo extends JiraUser {

    private String userName;

    private String userId;

    public String getUserName() {
        userName = getDisplayName();
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = getDisplayName();
    }

    public String getUserId() {
        userId = getName();
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = getName();
    }
}
