package com.mryx.matrix.process.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.process.dto.ProjectTaskDto;
import com.mryx.matrix.publish.domain.ProjectTask;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;


/**
 * 应用发布工单表
 *
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
@Data
public class Project extends BasePage implements Serializable {

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
     * 当前状态
     **/
    private Integer projectStatus;

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
     * 审核人姓名
     */
    private String auditor;

    /**
     * 审核状态 1：审核通过|0：未审核、驳回
     */
    private Integer auditStatus;

    /**
     * 项目下应用
     **/
    private List<ProjectTaskDto> projectTasks;

    /**
     * 鉴权用
     */
    private String accessToken;

    /**
     * 业务线-页面属性
     */
    private List<BusinessLine> businessLines;

    /**
     * 业务线-数据库属性，多个业务线用逗号分隔
     */
    private String businessLinesDb;

    /**
     * 是否为我的页面标识
     */
    private Boolean isMine;

    /**
     * 用于查询的用户名称
     */
    private String queryUserAccount;

    /**
     * 用于查询的用户中文名称
     */
    private String queryUserName;

    /**
     * 用户查询的任务ID列表
     */
    private List<String> queryProjectIds;

    /**
     * 关联的jira需求列表
     */
    private List<Issue> issueList;

    public List<BusinessLine> getBusinessLines() {
        if (businessLines == null) {
            businessLines = new ArrayList<>();
            if (!StringUtils.isEmpty(businessLinesDb)) {
                String[] lines = businessLinesDb.split(",");
                for (String line : lines) {
                    BusinessLine businessLine = new BusinessLine();
                    businessLine.setBusinessLineDesc(line);
                    businessLines.add(businessLine);
                }
            }
        }
        return businessLines;
    }

    public void setBusinessLines(List<BusinessLine> businessLines) {
        this.businessLines = businessLines;
    }

    public String getBusinessLinesDb() {
        return businessLinesDb;
    }

    public void setBusinessLinesDb(String businessLinesDb) {
        this.businessLinesDb = businessLinesDb;
    }

    public void setBusinessLinesDb(List<BusinessLine> businessLines) {
        if (!CollectionUtils.isEmpty(businessLines)) {
            Set<String> set = new HashSet<>();
            StringBuilder builder = new StringBuilder();
            businessLines.stream().forEach(businessLine -> {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                String businessLineDesc = businessLine.getBusinessLineDesc();
                if (set.contains(businessLineDesc)) {
                    return;
                }
                set.add(businessLineDesc);
                builder.append(businessLine.getBusinessLineDesc());
            });
            this.businessLinesDb = builder.toString();
        }
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", projectPriority=" + projectPriority +
                ", description='" + description + '\'' +
                ", projectType=" + projectType +
                ", projectStatus=" + projectStatus +
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
                ", auditor='" + auditor + '\'' +
                ", auditStatus=" + auditStatus +
                ", projectTasks=" + projectTasks +
                ", accessToken='" + accessToken + '\'' +
                ", businessLines=" + businessLines +
                ", businessLinesDb='" + businessLinesDb + '\'' +
                ", isMine=" + isMine +
                ", issueList=" + issueList +
                ", " + super.toString() +
                '}';
    }
}
