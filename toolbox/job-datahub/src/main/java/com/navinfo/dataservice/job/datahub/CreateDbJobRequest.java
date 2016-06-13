package com.navinfo.dataservice.job.datahub;

import com.alibaba.druid.util.StringUtils;
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
	protected String dbName;
	protected String userName;
	protected String bizType;
	protected String descp;
	protected String gdbVersion;
	protected String refDbName;
	protected String refUserName;
	protected String refBizType;

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

	public String getGdbVersion() {
		return gdbVersion;
	}

	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}

	public String getRefDbName() {
		return refDbName;
	}

	public void setRefDbName(String refDbName) {
		this.refDbName = refDbName;
	}

	public String getRefUserName() {
		return refUserName;
	}

	public void setRefUserName(String refUserName) {
		this.refUserName = refUserName;
	}

	public String getRefBizType() {
		return refBizType;
	}

	public void setRefBizType(String refBizType) {
		this.refBizType = refBizType;
	}

	@Override
	public int getStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 1;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#validate()
	 */
	@Override
	public void validate() throws JobException {
		if(StringUtils.isEmpty(dbName)){
			throw new JobException("create_db job请求参数错误：未设置dbNane属性");
		}
		if(StringUtils.isEmpty(userName)){
			throw new JobException("create_db job请求参数错误：未设置userName属性");
		}
		if(StringUtils.isEmpty(bizType)){
			throw new JobException("create_db job请求参数错误：未设置userName属性");
		}
	}

}
