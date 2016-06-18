package com.navinfo.dataservice.impcore.commit.day.poi;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：CommitDay2MonthPoiJob 请求参数的解析处理类
 * 
 */
public class Day2MonthPoiJobRequest extends AbstractJobRequest {
	private List<Integer> taskId;
	private String stopTime;//format:yyyymmddhh24miss
	
	public String getStopTime() {
		return stopTime;
	}
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}
	
	public List<Integer> getTaskId() {
		return taskId;
	}
	public void setTaskId(List<Integer> taskId) {
		this.taskId = taskId;
	}
	public Day2MonthPoiJobRequest() {
		super();
		log = LoggerRepos.getLogger(log);
	}
	public Day2MonthPoiJobRequest(JSONObject jsonConfig) {
		super();
		log = LoggerRepos.getLogger(log);
    	this.parseByJsonConfig(jsonConfig);
	}
	@Override
	public int getStepCount() throws JobException {
		return 1;
	}

	@Override
	public void validate() throws JobException {

	}

	

}

