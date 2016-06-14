package com.navinfo.dataservice.expcore.job;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.expcore.ExportConfig;
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
	protected String exportMode;
	protected int sourceDbId;
	protected String condition;
	protected List<String> conditionParams;
	protected String feature;
	protected boolean truncateData;
	protected boolean destroyTarget;
	protected int targetDbId;
	protected boolean multiThread4Input;
	protected boolean multiThread4Output;
	protected boolean dataIntegrity;
	protected Map<String,String> tableReNames;
	protected List<String> checkExistTables;
	protected String whenExist;
	protected List<String> specificTables;
	protected List<String> excludedTables;
	protected  List<String> flexTables;
	protected Map<String,String> flexConditions;
	

	@Override
	public int getStepCount() throws JobException {
		if(ExportConfig.MODE_COPY.equals(exportMode)){
			return 0;
		}else if(ExportConfig.MODE_FULL_COPY.equals(exportMode)){
			return 0;
		}else if(ExportConfig.MODE_FLEXIBLE.equals(exportMode)){
			return 0;
		}
		return 0;
	}

	@Override
	public void validate() throws JobException {
		if(ExportConfig.MODE_COPY.equals(exportMode)){
			
		}else if(ExportConfig.MODE_FULL_COPY.equals(exportMode)){
			
		}else if(ExportConfig.MODE_FLEXIBLE.equals(exportMode)){
			
		}
	}

	public String getExportMode() {
		return exportMode;
	}

	public void setExportMode(String exportMode) {
		this.exportMode = exportMode;
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

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public boolean isTruncateData() {
		return truncateData;
	}

	public void setTruncateData(boolean truncateData) {
		this.truncateData = truncateData;
	}

	public boolean isDestroyTarget() {
		return destroyTarget;
	}

	public void setDestroyTarget(boolean destroyTarget) {
		this.destroyTarget = destroyTarget;
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

}
