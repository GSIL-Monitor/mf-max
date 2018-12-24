package com.mryx.matrix.process.domain;
import java.io.Serializable;
import java.util.Date;


/**
 * 项目流程记录表
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-04 16:21
 **/
public class ProjectRecord extends BasePage implements Serializable {

	private static final long serialVersionUID = 7225367892378047852L;

	/**项目记录ID**/
	private Integer id;

	/**项目ID**/
	private Integer projectId;

	/**项目名称**/
	private String projectName;

	/**当前状态**/
	private Integer projectStatus;

	/**创建人**/
	private String createUser;

	/**更新人**/
	private String updateUser;

	/**创建时间**/
	private Date createTime;

	/**更新时间**/
	private Date updateTime;

	/**1:正常|0:删除**/
	private Integer delFlag;



	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setProjectId(Integer projectId){
		this.projectId = projectId;
	}

	public Integer getProjectId(){
		return this.projectId;
	}

	public void setProjectName(String projectName){
		this.projectName = projectName;
	}

	public String getProjectName(){
		return this.projectName;
	}

	public void setProjectStatus(Integer projectStatus){
		this.projectStatus = projectStatus;
	}

	public Integer getProjectStatus(){
		return this.projectStatus;
	}

	public void setCreateUser(String createUser){
		this.createUser = createUser;
	}

	public String getCreateUser(){
		return this.createUser;
	}

	public void setUpdateUser(String updateUser){
		this.updateUser = updateUser;
	}

	public String getUpdateUser(){
		return this.updateUser;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	public void setDelFlag(Integer delFlag){
		this.delFlag = delFlag;
	}

	public Integer getDelFlag(){
		return this.delFlag;
	}

	@Override
	public String toString() {
		return "ProjectRecord [ id= "+id+
			",projectId= "+projectId+
			",projectName= "+projectName+
			",projectStatus= "+projectStatus+
			",createUser= "+createUser+
			",updateUser= "+updateUser+
			",createTime= "+createTime+
			",updateTime= "+updateTime+
			",delFlag= "+delFlag+"]";
	}
}
