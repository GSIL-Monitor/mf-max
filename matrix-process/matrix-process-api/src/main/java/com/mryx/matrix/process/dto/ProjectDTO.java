package com.mryx.matrix.process.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.process.domain.BasePage;
import com.mryx.matrix.process.domain.BusinessLine;
import com.mryx.matrix.process.domain.Issue;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 应用发布工单表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
@Data
public class ProjectDTO extends BasePage implements Serializable {

    private static final long serialVersionUID = 8334484547086694254L;

    /**
     * 项目ID
     **/
    private Integer id;

    /**
     * 项目名称
     **/
    private String projectName;

    /**
     * 项目优先级
     **/
    private Integer projectPriority;

    /**
     * 信息描述
     **/
    private String description;

    /**
     * 需求类型
     **/
    private Integer projectType;

    /**
     * 需求类型
     **/
    private Integer projectTypeName;

    /**
     * 当前状态
     **/
    private Integer projectStatus;

    /**
     * 当前状态
     **/
    private String projectStatusName;

    /**
     * 产品负责人
     **/
    private String pmOwner;

    /**
     * 研发负责人
     **/
    private String devOwner;

    /**
     * 测试负责人
     **/
    private String qaOwner;

    /**
     * 项目参与人
     **/
    private String projectMember;

    /**
     * 计划开始时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date planStartTime;

    /**
     * 计划提测时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date planTestTime;

    /**
     * 计划发布时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date planPublishTime;

    /**
     * 实际开始时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date actualStartTime;

    /**
     * 实际提测时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date actualTestTime;

    /**
     * 实际发布时间
     **/
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date actualPublishTime;

    /**
     * 提测描述
     **/
    private String testDescribe;

    /**
     * 发布描述
     **/
    private String publishDescribe;

    /**
     * 创建人
     **/
    private String createUser;

    /**
     * 更新人
     **/
    private String updateUser;

    /**
     * 创建时间
     **/
    private Date createTime;

    /**
     * 更新时间
     **/
    private Date updateTime;

    /**
     * 1:正常|0:删除
     **/
    private Integer delFlag;

    /**
     * 项目下应用
     **/
    private List<ProjectTaskDto> projectTasks;

    /**
     * 审核用户
     */
    private String auditor;

    /**
     * 审核状态 1:审核通过|0:未审核、驳回
     */
    private Integer isAudit;

    public void setIsAudit(Integer isAudit) {
        this.isAudit = isAudit;
    }

    /**
     * 业务线,多个业务线用逗号分隔
     */
    private List<BusinessLine> businessLines;

    /**
     * 业务线，数据库映射
     */
    private String businessLinesDb;

    /**
     * 关联的jira需求列表
     */
    private List<Issue> issueList;

    @Override
    public String toString() {
        return "ProjectDTO [" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", projectPriority=" + projectPriority +
                ", description='" + description + '\'' +
                ", projectType=" + projectType +
                ", projectTypeName=" + projectTypeName +
                ", projectStatus=" + projectStatus +
                ", projectStatusName='" + projectStatusName + '\'' +
                ", pmOwner='" + pmOwner + '\'' +
                ", devOwner='" + devOwner + '\'' +
                ", qaOwner='" + qaOwner + '\'' +
                ", projectMember='" + projectMember + '\'' +
                ", planStartTime=" + planStartTime +
                ", planTestTime=" + planTestTime +
                ", planPublishTime=" + planPublishTime +
                ", actualStartTime=" + actualStartTime +
                ", actualTestTime=" + actualTestTime +
                ", actualPublishTime=" + actualPublishTime +
                ", testDescribe='" + testDescribe + '\'' +
                ", publishDescribe='" + publishDescribe + '\'' +
                ", createUser='" + createUser + '\'' +
                ", updateUser='" + updateUser + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", delFlag=" + delFlag +
                ", projectTasks=" + projectTasks +
                ", auditor='" + auditor + '\'' +
                ", isAudit=" + isAudit +
                ", businessLines=" + businessLines +
                ", businessLinesDb='" + businessLinesDb + '\'' +
                ']';
    }
}
