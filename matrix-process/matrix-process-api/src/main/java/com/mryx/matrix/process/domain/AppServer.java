package com.mryx.matrix.process.domain;

import com.mryx.matrix.common.domain.Base;
import com.mryx.matrix.common.domain.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 服务器对象
 *
 * @author pengcheng
 * @email pengcheng@missfresh.cn
 * @date 2018-10-19 16:32
 **/
@Data
public class AppServer extends Page implements Serializable {

    private static final long serialVersionUID = 1721717190137373258L;

    /**
     * 主键id
     **/
    private Integer id;

    /**
     * 主机iP(通过服务器资源表将serverid替换)
     **/
    private String hostIp;

    /**
     * 应用代号,全局唯一
     **/
    private String appCode;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 分组信息
     */
    private String groupName;
    /**
     * 创建时间
     **/
    private Date gmtCreate;

    /**
     * 修改时间
     **/
    private Date gmtModified;

    /**
     * 生成的tag
     **/
    private String tag;

    /**
     * 发布状态
     **/
    private Integer status;

    /**
     * 删除标识:0已删除;1未删除
     */
    private Integer delFlag;

    /**
     * Agent最后上报时间,用毫秒数表示
     */
    private Long msReport;

    /**
     * Agent状态
     */
    private Integer agentStatus;

    @Override
    public String toString() {
        return "AppServer{" +
                "id=" + id +
                ", hostIp='" + hostIp + '\'' +
                ", appCode='" + appCode + '\'' +
                ", appName='" + appName + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", tag='" + tag + '\'' +
                ", status=" + status +
                ", delFlag=" + delFlag +
                ", msReport=" + msReport +
                ", agentStatus=" + agentStatus +
                ", " + super.toString() +
                '}';
    }
}
