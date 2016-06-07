package com.navinfo.dataservice.jobframework.commit;

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
	
	public List<Integer> getGridList() {
		return gridList;
	}
	public void setGridSet(List<Integer> gridSet) {
		this.gridList = gridSet;
	}
	public CommitDay2MonthRoadJobRequest(JSONObject jsonConfig) {
		super();
		log = LoggerRepos.getLogger(log);
    	this.parseByJsonConfig(jsonConfig);
	}
	@Override
	public int getStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void validate() throws JobException {
		

	}

	

}

