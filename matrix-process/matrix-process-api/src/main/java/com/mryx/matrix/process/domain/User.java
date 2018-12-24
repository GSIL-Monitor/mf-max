package com.mryx.matrix.process.domain;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-05 13:18
 **/
public class User implements Serializable {

	private static final long serialVersionUID = 7754444171077836464L;

	/****/
	private Integer id;

	/**用户名**/
	private String userName;

	/**密码**/
	private String password;

	/**创建时间**/
	private Date createTime;



	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return this.userName;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString() {
		return "User [ id= "+id+
			",userName= "+userName+
			",password= "+password+
			",createTime= "+createTime+"]";
	}
}
