package com.navinfo.dataservice.column.job;

import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: Fm2MultiSrcSyncJobRequest
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: Fm2MultiSrcSyncJobRequest.java
 */
public class Fm2MultiSrcSyncJobRequest extends AbstractJobRequest {
	
	protected List<Integer> dbIds;
	protected String lastSyncTime;
	protected String syncTime;

	public List<Integer> getDbIds() {
		return dbIds;
	}

	public void setDbIds(List<Integer> dbIds) {
		this.dbIds = dbIds;
	}

	public String getLastSyncTime() {
		return lastSyncTime;
	}

	public void setLastSyncTime(String lastSyncTime) {
		this.lastSyncTime = lastSyncTime;
	}

	public String getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(String syncTime) {
		this.syncTime = syncTime;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "fm2MultiSrcSync";
	}

	@Override
	public String getJobTypeName() {
		return "创建FM日库多源增量包";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

	@Override
	public void validate() throws JobException {
		try{
			if(dbIds==null||dbIds.size()==0)throw new JobException("传入大区库不能为空");
			if(syncTime==null)throw new JobException("同步截止时间不能为空");
			if(lastSyncTime!=null){
				Date lastDate=DateUtils.stringToDate(lastSyncTime, DateUtils.DATE_COMPACTED_FORMAT);
				Date syncDate=DateUtils.stringToDate(syncTime, DateUtils.DATE_COMPACTED_FORMAT);
				if(lastDate.getTime()>syncDate.getTime()){
					throw new JobException("同步截止时间不能早于起始时间");
				}
			}
		}catch(JobException e){
			throw e;
		}catch(Exception ex){
			log.error(ex.getMessage(),ex);
			throw new JobException("job参数验证不通过："+ex.getMessage(),ex);
		}
	}

}
