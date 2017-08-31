package com.navinfo.dataservice.charge.job;

import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/**
 * FM导入桩家的增量照片数据包
 * @ClassName Fm2ChargePhotoAddRequest
 * @author Han Shaoming
 * @date 2017年8月25日 下午2:31:30
 * @Description TODO
 */
public class Fm2ChargePhotoAddRequest extends AbstractJobRequest {

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
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "fm2ChargePhotoAdd";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "FM导入桩家库照片增量包生成";
	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void validate() throws JobException {
		try{
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
