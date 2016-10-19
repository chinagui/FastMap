package com.navinfo.dataservice.impcore.release.day;

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
 * {"gridList":[34234,234324],
 *  "featureType":"ALL"//POI,ROAD
 * }
 */
public class ReleaseFmIdbDailyJobRequest extends AbstractJobRequest {
	private List<Integer> gridList;
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
		return "releaseFmIdbDailyJob";
	}

	@Override
	public String getJobTypeName(){
		return "日出品提交";
	}
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 1;
	}

	

}

