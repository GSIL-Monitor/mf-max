package com.mryx.matrix.process.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mryx.matrix.process.domain.BusinessLine;
import com.mryx.matrix.process.dto.ProjectTaskDto;
import com.mryx.matrix.process.web.param.BaseParam;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
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
public class ProjectVO extends BaseParam implements Serializable {

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
     * 项目优先级
     **/
    private String projectPriorityDesc;


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
    private String projectTypeDesc;

    /**
     * 当前状态
     **/
    private Integer projectStatus;

    /**
     * 当前状态
     **/
    private String projectStatusDesc;

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
     * 业务线,多个业务线用逗号分隔
     */
    private List<BusinessLine> businessLines;

    /**
     * 业务线，数据库映射
     */
    private String businessLinesDb;

    public List<BusinessLine> getBusinessLines() {
        if (businessLines == null) {
            if (!StringUtils.isEmpty(businessLinesDb)) {
                String[] lines = businessLinesDb.split(",");
                businessLines = new ArrayList<>();
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

}
