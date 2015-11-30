package com.navinfo.dataservice.datahub.model;

/** 
 * @ClassName: OracleSchema 
 * @author Xiao Xiaowen 
 * @date 2015-11-26 下午8:08:01 
 * @Description: TODO
 */
public class OracleSchema extends AbstractDb {
	private String userName;
	private String passwd;
	private boolean isSysdba;
	private String tablespaceName;
	
	
	

	@Override
	public boolean isAdminDb() throws Exception {
		return isSysdba;
	}
	@Override
	public AbstractDb getAdminDb()throws Exception{
		return null;
	}
	@Override
	public boolean create(AbstractDb adminDb)throws Exception{
		return false;
	}
	
/* getter setter */
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public boolean isSysdba() {
		return isSysdba;
	}
	public void setSysdba(boolean isSysdba) {
		this.isSysdba = isSysdba;
	}
	public String getTablespaceName() {
		return tablespaceName;
	}
	public void setTablespaceName(String tablespaceName) {
		this.tablespaceName = tablespaceName;
	}
	
}
