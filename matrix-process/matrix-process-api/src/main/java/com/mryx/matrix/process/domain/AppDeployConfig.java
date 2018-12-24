package com.mryx.matrix.process.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
@Data
public class AppDeployConfig implements Serializable {

    private static final long serialVersionUID = 6685738069042426751L;

    /**
     * 主键id
     **/
    private Integer id;

    /**
     * 应用代号,全局唯一
     **/
    private String appCode;

    /**
     * 环境配置 dev beta prod
     **/
    private String appEnv;

    /**
     * git 地址
     **/
    private String git;

    /**
     * 部署路径
     **/
    private String deployPath;

    /**
     * healthcheck地址
     **/
    private String healthcheck;

    /**
     * 部署参数
     **/
    private String deployParameters;

    /**
     * 包名称
     **/
    private String pkgName;

    /**
     * 包类型 jar war go h5 py
     **/
    private String pkgType;

    /**
     * 部署端口号
     **/
    private Integer port;

    /**
     * 修改时间
     **/
    private Date gmtModified;

    /**
     * 删除标识，1:正常|0:删除
     */
    private int delFlag;

    /**
     * 虚拟机参数
     */
    private String vmOption;

    @Override
    public String toString() {
        return "AppDeployConfig{" +
                "id=" + id +
                ", appCode='" + appCode + '\'' +
                ", appEnv='" + appEnv + '\'' +
                ", git='" + git + '\'' +
                ", deployPath='" + deployPath + '\'' +
                ", healthcheck='" + healthcheck + '\'' +
                ", deployParameters='" + deployParameters + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", pkgType='" + pkgType + '\'' +
                ", port=" + port +
                ", gmtModified=" + gmtModified +
                ", delFlag=" + delFlag +
                ", vmOption='" + vmOption + '\'' +
                '}';
    }
}
