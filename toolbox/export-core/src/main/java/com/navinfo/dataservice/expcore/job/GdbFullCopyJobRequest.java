package com.navinfo.dataservice.expcore.job;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: GdbFullCopyJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月14日 下午5:58:23 
* @Description: TODO
*  
*/
public class GdbFullCopyJobRequest extends AbstractJobRequest {
	protected int sourceDbId;
//	protected String feature;
	protected int targetDbId;
	protected boolean multiThread4Output=true;
	protected Map<String,String> tableReNames;
	protected List<String> specificTables;
	protected List<String> excludedTables;

	@Override
	public int getStepCount() throws JobException {
		return 0;
	}


	@Override
	public void validate() throws JobException {
		
	}


	public int getSourceDbId() {
		return sourceDbId;
	}


	public void setSourceDbId(int sourceDbId) {
		this.sourceDbId = sourceDbId;
	}

	public int getTargetDbId() {
		return targetDbId;
	}


	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}


	public boolean isMultiThread4Output() {
		return multiThread4Output;
	}


	public void setMultiThread4Output(boolean multiThread4Output) {
		this.multiThread4Output = multiThread4Output;
	}


	public Map<String, String> getTableReNames() {
		return tableReNames;
	}


	public void setTableReNames(Map<String, String> tableReNames) {
		this.tableReNames = tableReNames;
	}


	public List<String> getSpecificTables() {
		return specificTables;
	}


	public void setSpecificTables(List<String> specificTables) {
		this.specificTables = specificTables;
	}


	public List<String> getExcludedTables() {
		return excludedTables;
	}


	public void setExcludedTables(List<String> excludedTables) {
		this.excludedTables = excludedTables;
	}

}
