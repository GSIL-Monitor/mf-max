package com.mryx.matrix.codeanalyzer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目应用测试实体类
 */
@Data
public class ProjectCodeScanTask extends Page implements Serializable {
    private static final long serialVersionUID = -8787333715310899209L;

    /*任务ID*/
    private Integer id;
    /*任务名称*/
    private String taskName;
    /*被测应用*/
    private String appCode;
    /*扫描类型*/
    private Integer modeOfScan;
    /*代码分支*/
    private String appBranch;
    /*基线版本*/
    private String baseVersion;
    /*对比版本*/
    private String compareVersion;
    /*定时任务触发时间*/
    private String timeTrigger;
    /*前端传过来的token，用此token调用接口查询创建人姓名*/
    private String accessToken;
    /*创建人姓名*/
    private String userName;

    /*唯一标识，用于join链接*/
    private String jobId;

    /*创建时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /*更新时间*/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public ProjectCodeScanTask() {
    }

    public ProjectCodeScanTask(Integer id, String taskName, String appCode, Integer modeOfScan, String appBranch, String baseVersion, String compareVersion, String timeTrigger, String accessToken, String userName, Date createTime, Date updateTime) {
        super();
        this.id = id;
        this.taskName = taskName;
        this.appCode = appCode;
        this.modeOfScan = modeOfScan;
        this.appBranch = appBranch;
        this.baseVersion = baseVersion;
        this.compareVersion = compareVersion;
        this.timeTrigger = timeTrigger;
        this.accessToken = accessToken;
        this.userName = userName;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ProjectCodeScanTask{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", appCode='" + appCode + '\'' +
                ", typeOfScan=" + modeOfScan +
                ", appBranch='" + appBranch + '\'' +
                ", baseVersion='" + baseVersion + '\'' +
                ", compareVersion='" + compareVersion + '\'' +
                ", timeTrigger='" + timeTrigger + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", userName='" + userName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
