package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import java.util.List;

/**
 * @ClassName: RoadDtoMCoreJobRequest
 * @author Zhang Runze
 * @date 2016年6月21日 上午11:57:17
 * @Description: TODO：道路日落月核心Job参数封装
 *
 */
public class RoadDtoMCoreJobRequest extends AbstractJobRequest {

	private int executeDBId;
	private int dayDBId;

	@Override
	public void defineSubJobRequests() throws JobCreateException {

	}

	@Override
	protected int myStepCount() throws JobException {
		return 2;
	}

	@Override
	public String getJobType() {
		return "roadDtoMCore";
	}
	
	@Override
	public String getJobTypeName(){
		return "道路日落月（核心）";
	}

	@Override
	public void validate() throws JobException {
	}

	public int getExecuteDBId() {
		return executeDBId;
	}

	public void setExecuteDBId(int executeDBId) {
		this.executeDBId = executeDBId;
	}


	public int getDayDBId() { return dayDBId; }

	public void setDayDBId(int dayDBId) { this.dayDBId = dayDBId; }

}
