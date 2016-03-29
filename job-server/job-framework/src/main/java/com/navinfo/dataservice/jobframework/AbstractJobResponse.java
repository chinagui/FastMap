package com.navinfo.dataservice.jobframework;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobRuntimeException;

/** 
 * @ClassName: AbstractJobRequest 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午2:18:48 
 * @Description: TODO
 */
public abstract class AbstractJobResponse {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected int status = 0;
	protected String msg ="";
	public static int STATUS_FAILED=-1;
	public static int STATUS_INIT=0;
	public static int STATUS_SUCCESS=1;
	
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setStatusAndMsg(int status,String msg){
		this.status=status;
		this.msg=msg;
	}
	/**
	 * 为了简便，所有属性值全部使用字符串类型
	 * @throws JobRuntimeException
	 */
	protected abstract JSONObject generateDataJson()throws JobRuntimeException;
	
	public JSONObject generateJson()throws JobRuntimeException{
		JSONObject res = new JSONObject();
		res.put("status", String.valueOf(status));
		res.put("msg", msg);
		res.put("data", generateDataJson());
		return res;
	}
}
