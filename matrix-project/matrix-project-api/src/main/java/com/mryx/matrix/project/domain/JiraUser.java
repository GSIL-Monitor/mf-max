package com.mryx.matrix.project.domain;

import com.mryx.matrix.common.domain.Base;
import lombok.Data;

/**
 * @author pengcheng
 * @description Jira用户
 * @email pengcheng@missfresh.cn
 * @date 2018-11-13 18:05
 **/
@Data
public class JiraUser extends Base {
    private String self;
    private String key;
    private String name;
    private String emailAddress;
    private String displayName;
}
