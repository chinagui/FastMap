package com.navinfo.dataservice.impcore.release.mon;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：CommitDay2MonthRoadJob 请求参数的解析处理类
 * {"gridList":[234234,324324],
 * "stopTime":"yyyymmddHH24miss"
 * }
 * 
 */
public class ReleaseFmIdbMonthlyJobRequest extends AbstractJobRequest {
	private List<Integer> gridList;
	private String stopTime;//format:yyyymmddhh24miss
	
	public String getStopTime() {
		return stopTime;
	}
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}
	public List<Integer> getGridList() {
		return gridList;
	}
	public void setGridList(List<Integer> gridList) {
		this.gridList = gridList;
	}

	@Override
	public void validate() throws JobException {

	}
	@Override
	public String getJobType() {
		return "releaseFmIdbMonthlyJob";
	}

	@Override
	public String getJobTypeName(){
		return "月库提交";
	}
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 2;
	}

	

}

