package com.navinfo.navicommons.utils.user.model;

/**
 * 用户
*/
public class AuthUser {
	
	
	protected Integer userId;
	
	protected String userName;
	
	protected String password;
	
	/*描叙*/
	protected String decs;
	
	/*部门名称*/
	protected String department;
	
	/*电话*/
	protected String telephone;
	
	/*邮箱*/
	protected String eMail;
	
	/*中文名*/
	protected String userNameCn;
	
	/*明文密码*/
	protected String plainpassword;
	
	
	
	
	/**
	 * ------------------------getter,setter-------------------------------------
	 */
	
	public Integer getUserId(){
		return this.userId;
	}
	
	public void setUserId(Integer userId){
		this.userId=userId;
	}
	
	public String getUserName(){
		return this.userName;
	}
	public void setUserName(String userName){
		this.userName=userName;
	}
	
	public String getPassword(){
		return this.password;
	}
	public void setPassword(String password){
		this.password=password;
	}
	
	public String getDecs(){
		return this.decs;
	}
	public void setDecs(String decs){
		this.decs=decs;
	}
	
	public String getDepartment(){
		return this.department;
	}
	public void setDepartment(String department){
		this.department=department;
	}
	
	public String getTelephone(){
		return this.telephone;
	}
	public void setTelephone(String telephone){
		this.telephone=telephone;
	}
	
	public String getEMail(){
		return this.eMail;
	}
	public void setEMail(String eMail){
		this.eMail=eMail;
	}
	
	public String getUserNameCn(){
		return this.userNameCn;
	}
	public void setUserNameCn(String userNameCn){
		this.userNameCn=userNameCn;
	}
	
	public String getPlainpassword(){
		return this.plainpassword;
	}
	public void setPlainpassword(String plainpassword){
		this.plainpassword=plainpassword;
	}
	
	/*toString()*/
	public String toString(){
		
		return "userId:"+userId+","+"userName:"+userName+","+"password:"+password+","+"decs:"+decs+","+"department:"+department+","+"telephone:"+telephone+","+"eMail:"+eMail+","+"userNameCn:"+userNameCn+","+"plainpassword:"+plainpassword+","+"";
	}
}
