package com.navinfo.dataservice.expcore.job;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: GdbExportJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月14日 上午9:48:33 
* @Description: TODO
*  
*/
public class GdbExportJobRequest extends AbstractJobRequest {
	protected int sourceDbId;
	protected String condition=ExportConfig.CONDITION_BY_MESH;
	protected List<String> conditionParams;
	protected int meshExtendCount=0;
	protected String featureType=ExportConfig.FEATURE_ALL;//poi,road,all,GlmTable.FEATURE_TYPE_XXX
	protected String mode=ExportConfig.MODE_COPY;
//	protected List<String> objTypes;
//	protected boolean deleteData;
//	protected boolean destroyTarget;
	protected int targetDbId;
	protected boolean multiThread4Input=true;
	protected boolean multiThread4Output=true;
	protected boolean dataIntegrity;
	protected Map<String,String> tableReNames;
	protected List<String> checkExistTables;
	protected String whenExist;
	protected List<String> specificTables;
	protected List<String> excludedTables;
	protected  List<String> flexTables;
	protected Map<String,String> flexConditions;

	@Override
	public void validate() throws JobException {
		
	}

	public int getSourceDbId() {
		return sourceDbId;
	}

	public void setSourceDbId(int sourceDbId) {
		this.sourceDbId = sourceDbId;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public List<String> getConditionParams() {
		return conditionParams;
	}

	public void setConditionParams(List<String> conditionParams) {
		this.conditionParams = conditionParams;
	}

	public int getMeshExtendCount() {
		return meshExtendCount;
	}

	public void setMeshExtendCount(int meshExtendCount) {
		this.meshExtendCount = meshExtendCount;
	}

	public String getFeatureType() {
		return featureType;
	}

	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public boolean isMultiThread4Input() {
		return multiThread4Input;
	}

	public void setMultiThread4Input(boolean multiThread4Input) {
		this.multiThread4Input = multiThread4Input;
	}

	public boolean isMultiThread4Output() {
		return multiThread4Output;
	}

	public void setMultiThread4Output(boolean multiThread4Output) {
		this.multiThread4Output = multiThread4Output;
	}

	public boolean isDataIntegrity() {
		return dataIntegrity;
	}

	public void setDataIntegrity(boolean dataIntegrity) {
		this.dataIntegrity = dataIntegrity;
	}

	public Map<String, String> getTableReNames() {
		return tableReNames;
	}

	public void setTableReNames(Map<String, String> tableReNames) {
		this.tableReNames = tableReNames;
	}

	public List<String> getCheckExistTables() {
		return checkExistTables;
	}

	public void setCheckExistTables(List<String> checkExistTables) {
		this.checkExistTables = checkExistTables;
	}

	public String getWhenExist() {
		return whenExist;
	}

	public void setWhenExist(String whenExist) {
		this.whenExist = whenExist;
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

	public List<String> getFlexTables() {
		return flexTables;
	}

	public void setFlexTables(List<String> flexTables) {
		this.flexTables = flexTables;
	}

	public Map<String, String> getFlexConditions() {
		return flexConditions;
	}

	public void setFlexConditions(Map<String, String> flexConditions) {
		this.flexConditions = flexConditions;
	}

	@Override
	public String getJobType() {
		return "gdbExport";
	}

	@Override
	public String getJobTypeName(){
		return "GDB导出";
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

}
