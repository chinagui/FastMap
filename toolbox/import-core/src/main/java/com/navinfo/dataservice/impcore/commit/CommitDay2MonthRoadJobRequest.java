package com.navinfo.dataservice.impcore.commit;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：CommitDay2MonthRoadJob 请求参数的解析处理类
 * 
 */
public class CommitDay2MonthRoadJobRequest extends AbstractJobRequest {
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
	public void setGridSet(List<Integer> gridSet) {
		this.gridList = gridSet;
	}
	public CommitDay2MonthRoadJobRequest() {
		super();
		log = LoggerRepos.getLogger(log);
	}
	public CommitDay2MonthRoadJobRequest(JSONObject jsonConfig) {
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

