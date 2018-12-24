package com.mryx.matrix.project.vo;

import com.mryx.matrix.common.domain.Base;
import lombok.Data;

/**
 * @author pengcheng
 * @description
 * @email pengcheng@missfresh.cn
 * @date 2018-12-11 21:16
 **/
@Data
public class TransitionVo extends Base {

    /**
     * matrix 任务ID
     */
    private Integer projectId;

    /**
     * 流转ID
     */
    private String id;

    /**
     * 流转名称
     */
    private String name;

    /**
     * review 负责人
     */
    private String reviewer;

    /**
     * 技术文档
     */
    private String techDocument;

    /**
     * 经办人
     */
    private String assignee;

    /**
     * 计划评审完成时间
     */
    private String planReviewCompleteDate;

    /**
     * 计划排期时间
     */
    private String planScheduleDate;

    /**
     * 计划提测时间
     */
    private String planTestDate;

    /**
     * 计划上线时间
     */
    private String planPublishDate;

    /**
     * 备注
     */
    private String comment;

    @Override
    public String toString() {
        return "TransitionVo{" +
                "projectId=" + projectId +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", review='" + reviewer + '\'' +
                ", techDocument='" + techDocument + '\'' +
                ", assignee='" + assignee + '\'' +
                ", planReviewCompleteDate='" + planReviewCompleteDate + '\'' +
                ", planScheduleDate='" + planScheduleDate + '\'' +
                ", planTestDate='" + planTestDate + '\'' +
                ", planPublishDate='" + planPublishDate + '\'' +
                ", comment='" + comment + '\'' +
                ", " + super.toString() +
                '}';
    }
}
