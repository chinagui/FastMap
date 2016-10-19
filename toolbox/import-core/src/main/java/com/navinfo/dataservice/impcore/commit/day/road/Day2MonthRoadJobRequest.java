package com.navinfo.dataservice.impcore.commit.day.road;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：CommitDay2MonthRoadJob 请求参数的解析处理类
 * 
 */
public class Day2MonthRoadJobRequest extends AbstractJobRequest {
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
	public Day2MonthRoadJobRequest() {
		super();
		log = LoggerRepos.getLogger(log);
	}

	@Override
	public void validate() throws JobException {

	}
	@Override
	public String getJobType() {
		return "day2MonthRoadJob";
	}
	@Override
	public String getJobTypeName(){
		return "道路日落月";
	}
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	

}

