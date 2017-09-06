package com.navinfo.dataservice.charge.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/**
 * FM导入桩家的初始化照片数据包
 * @ClassName Fm2ChargePhotoInitRequest
 * @author Han Shaoming
 * @date 2017年8月25日 下午2:27:16
 * @Description TODO
 */
public class Fm2ChargePhotoInitRequest extends AbstractJobRequest {

	protected List<Integer> dbIds;
	
	public List<Integer> getDbIds() {
		return dbIds;
	}

	public void setDbIds(List<Integer> dbIds) {
		this.dbIds = dbIds;
	}
	
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "fm2ChargePhotoInit";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "FM导入桩家库照片初始化包生成";
	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

}
