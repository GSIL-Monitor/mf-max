package com.mryx.matrix.process.dto;

import com.mryx.matrix.process.domain.AppServer;
import lombok.Data;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-05 16:24
 **/
@Data
public class AppServerDTO extends AppServer {

    /**
     * 应用代码模糊搜索
     */
    private String appCodeLike;

    /**
     * 应用名称模糊搜索
     */
    private String appNameLike;

    /**
     * 主机iP(模糊搜索)
     **/
    private String hostIpLike;

    @Override
    public String toString() {
        return "AppServerDTO{" +
                "appCodeLike='" + appCodeLike + '\'' +
                ", appNameLike='" + appNameLike + '\'' +
                ", hostIpLike='" + hostIpLike + '\'' +
                ", " + super.toString() +
                '}';
    }

}
