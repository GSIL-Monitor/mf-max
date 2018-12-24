package com.mryx.matrix.process.domain;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 周报表
 * @author juqing
 * @email jvqing@missfresh.cn
 * @date 2018-09-05 15:14
 **/
public class ReportDTO extends BasePage implements Serializable {


	private static final long serialVersionUID = -4632945391340373109L;
	/**id**/
	private Integer id;

	/**周报时间**/
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
	private Date reportTime;

	private Date createTime;

	private Date updateTime;

	/**自定义项目名称**/
	private String selfProjectName;

	/**本周内容**/
	private String curWeekCnt;

	/**下周计划**/
	private String nextWeekCnt;

	/**记录人**/
	private String author;

	/**0已删除 1未删除**/
	private Integer delFlag;



	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setReportTime(Date reportTime){
		this.reportTime = reportTime;
	}

	public Date getReportTime(){
		return this.reportTime;
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

	public void setSelfProjectName(String selfProjectName){
		this.selfProjectName = selfProjectName;
	}

	public String getSelfProjectName(){
		return this.selfProjectName;
	}

	public void setCurWeekCnt(String curWeekCnt){
		this.curWeekCnt = curWeekCnt;
	}

	public String getCurWeekCnt(){
		return this.curWeekCnt;
	}

	public void setNextWeekCnt(String nextWeekCnt){
		this.nextWeekCnt = nextWeekCnt;
	}

	public String getNextWeekCnt(){
		return this.nextWeekCnt;
	}

	public void setAuthor(String author){
		this.author = author;
	}

	public String getAuthor(){
		return this.author;
	}

	public void setDelFlag(Integer delFlag){
		this.delFlag = delFlag;
	}

	public Integer getDelFlag(){
		return this.delFlag;
	}

	@Override
	public String toString() {
		return "ReportDTO [ id= "+id+
			",reportTime= "+reportTime+
			",createTime= "+createTime+
			",updateTime= "+updateTime+
			",selfProjectName= "+selfProjectName+
			",curWeekCnt= "+curWeekCnt+
			",nextWeekCnt= "+nextWeekCnt+
			",author= "+author+
			",delFlag= "+delFlag+"]";
	}
}
