package com.navinfo.dataservice.impcore.release.day;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：CommitDay2MonthRoadJob 请求参数的解析处理类
 * {"gridList":[34234,234324],
 *  "stopTime":"yyyymmddHH24miss",
 *  "featureType":"ALL"//POI,ROAD
 * }
 */
public class ReleaseFmIdbDailyJobRequest extends AbstractJobRequest {
	private List<Integer> gridList;
	private String stopTime;//format:yyyymmddhh24miss
	private String featureType;//参考LogFlusher.FEATURE_POI
	
	/**要素类型：ALL：全要素(POI+ROAD);POI:只POI;ROAD:只ROAD
	 * @return 参考LogFlusher.FEATURE_POI FEATURE_ALL  FEATURE_ROAD
	 */
	public String getFeatureType() {
		return featureType;
	}
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	public String getStopTime() {
		return stopTime;
	}
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}
	public List<Integer> getGridList() {
		return gridList;
	}
	public void setGridList(List<Integer> gridSet) {
		this.gridList = gridSet;
	}
	public ReleaseFmIdbDailyJobRequest() {
		super();
		log = LoggerRepos.getLogger(log);
	}
	public ReleaseFmIdbDailyJobRequest(JSONObject jsonConfig) {
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
	@Override
	public String getJobType() {
		return "releaseFmIdbDailyJob";
	}

	

}

