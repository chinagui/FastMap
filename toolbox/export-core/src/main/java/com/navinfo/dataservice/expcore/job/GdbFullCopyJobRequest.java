package com.navinfo.dataservice.expcore.job;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
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
	protected int sourceDbId=0;
	protected String sourceDbInfo;//"ORALCE,ip,port,sid,user name,user passwd"
	protected String featureType;//poi,road,all,GlmTable.FEATURE_TYPE_XXX
	protected boolean truncateData=false;//导入数据之前，是否清空目标库，只会清空导出目标表
	protected int targetDbId=0;
	protected String targetDbInfo;//"ORACLE,ip,port,sid,user name,user passwd"
	protected boolean multiThread4Output=true;
	protected Map<String,String> tableReNames;
	protected List<String> specificTables;
	protected List<String> excludedTables;

	@Override
	public void validate() throws JobException {
		
	}


	public int getSourceDbId() {
		return sourceDbId;
	}


	public void setSourceDbId(int sourceDbId) {
		this.sourceDbId = sourceDbId;
	}

	public boolean isTruncateData() {
		return truncateData;
	}


	public void setTruncateData(boolean truncateData) {
		this.truncateData = truncateData;
	}


	public String getFeatureType() {
		return featureType;
	}


	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}


	public int getTargetDbId() {
		return targetDbId;
	}


	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}


	public String getSourceDbInfo() {
		return sourceDbInfo;
	}


	public void setSourceDbInfo(String sourceDbInfo) {
		this.sourceDbInfo = sourceDbInfo;
	}


	public String getTargetDbInfo() {
		return targetDbInfo;
	}


	public void setTargetDbInfo(String targetDbInfo) {
		this.targetDbInfo = targetDbInfo;
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


	@Override
	public String getJobType() {
		return "gdbFullCopy";
	}

	@Override
	public String getJobTypeName(){
		return "GDB复制";
	}


	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected int myStepCount() throws JobException {
		int count =2;
		if(truncateData){
			count+=1;
		}
		return count;
	}

}
