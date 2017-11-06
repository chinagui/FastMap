package com.navinfo.dataservice.column.job;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import net.sf.json.JSONObject;

/** 
 * @ClassName: InfoPoiMultiSrc2FmDayJobRequest
 * @author zl
 * @date 2017年11月1日
 * @Description: InfoPoiMultiSrc2FmDayJobRequest.java
 */
public class InfoPoiMultiSrc2FmDayJobRequest extends AbstractJobRequest {
	
	protected int dbId ;
	protected int taskId ;
	protected int subtaskId ;
	protected String bSourceId ;
	protected JSONObject data;
	
	

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getSubtaskId() {
		return subtaskId;
	}

	public void setSubtaskId(int subtaskId) {
		this.subtaskId = subtaskId;
	}

	public String getBSourceId() {
		return bSourceId;
	}

	public void setBSourceId(String bSourceId) {
		this.bSourceId = bSourceId;
	}

	public JSONObject getData() {
		return data;
	}

	public void setData(JSONObject data) {
		this.data = data;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	public String getJobType() {
		return "infoPoiMultiSrc2FmDay";
	}

	@Override
	public String getJobTypeName() {
		return "一级数据型poi情报入日库";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

	@Override
	public void validate() throws JobException {
		try{
			if(dbId==0)throw new JobException("传入大区库不能为空.");
			if(taskId==0)throw new JobException("传入任务号不能为空.");
			if(subtaskId==0)throw new JobException("传入子任务号不能为空.");
			if(bSourceId==null || StringUtils.isEmpty(bSourceId))throw new JobException("传入bSourceId不能为空.");
			if(data==null){
					throw new JobException("传入poi数据不能为空.");
			}
		}catch(JobException e){
			throw e;
		}catch(Exception ex){
			log.error(ex.getMessage(),ex);
			throw new JobException("job参数验证不通过："+ex.getMessage(),ex);
		}
	}

}
