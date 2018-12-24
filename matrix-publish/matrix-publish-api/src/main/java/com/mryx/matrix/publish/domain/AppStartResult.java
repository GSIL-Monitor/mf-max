package com.mryx.matrix.publish.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * AppStartResult
 *
 * @author supeng
 * @date 2018/11/17
 */
@Data
public class AppStartResult {
    /**
     * ID
     */
    private Integer id;
    /**
     * 成功/失败Code
     */
    private String resultCode;
    /**
     * 成功/失败信息
     */
    private String message;
    /**
     * 发布类型
     */
    private String buildType;
    /**
     * 应用机器Ip
     */
    private String ip;
    /**
     * 发布记录ID
     */
    private String recordId;
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
