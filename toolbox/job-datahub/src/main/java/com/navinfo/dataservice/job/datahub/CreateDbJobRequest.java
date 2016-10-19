package com.navinfo.dataservice.job.datahub;

import com.alibaba.druid.util.StringUtils;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: CreateDbJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月12日 下午4:36:47 
* @Description: TODO
*  
*/
public class CreateDbJobRequest extends AbstractJobRequest {
	protected String serverType;
	protected String dbName;
	protected String userName;
	protected String userPasswd;
	protected String bizType;
	protected String descp;
	protected int refDbId;

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPasswd() {
		return userPasswd;
	}

	public void setUserPasswd(String userPasswd) {
		this.userPasswd = userPasswd;
	}

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public String getDescp() {
		return descp;
	}

	public void setDescp(String descp) {
		this.descp = descp;
	}


	public int getRefDbId() {
		return refDbId;
	}

	public void setRefDbId(int refDbId) {
		this.refDbId = refDbId;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#validate()
	 */
	@Override
	public void validate() throws JobException {
		if(StringUtils.isEmpty(serverType)){
			throw new JobException("create_db job请求参数错误：serverType库类型未设置");
		}
		if(StringUtils.isEmpty(bizType)){
			throw new JobException("create_db job请求参数错误：未设置bizType属性");
		}
	}

	@Override
	public String getJobType() {
		return "createDb";
	}
	@Override
	public String getJobTypeName(){
		return "创建库";
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 1;
	}

}
