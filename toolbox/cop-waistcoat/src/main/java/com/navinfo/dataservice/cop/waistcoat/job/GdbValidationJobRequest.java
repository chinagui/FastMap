package com.navinfo.dataservice.cop.waistcoat.job;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.api.datahub.model.BizType;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

/** 
* @ClassName: GdbValidationJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月21日 下午3:52:48 
* @Description: TODO
*  
*/
public class GdbValidationJobRequest extends AbstractJobRequest {
	protected List<Integer> grids;
	protected List<String> rules;
	protected int targetDbId;
	protected int valDbId=0;
	protected int timeOut;
	protected boolean reuseDb=true;
	
	@Override
	public String getJobType() {
		return "gdbValidation";
	}

	@Override
	public String getJobTypeName(){
		return "检查";
	}

	@Override
	public void validate() throws JobException {
		if(valDbId<1&&(this.getSubJobRequest("createValDb")==null||this.getSubJobRequest("expValDb")==null)){
			throw new JobException("检查的子版本库未指定，且未指定新创建方式。");
		}
	}

	public List<Integer> getGrids() {
		return grids;
	}

	public void setGrids(List<Integer> grids) {
		this.grids = grids;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public int getValDbId() {
		return valDbId;
	}

	public void setValDbId(int valDbId) {
		this.valDbId = valDbId;
	}


	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public boolean isReuseDb() {
		return reuseDb;
	}

	public void setReuseDb(boolean reuseDb) {
		this.reuseDb = reuseDb;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		//createBatchDb
		if(valDbId==0){
			AbstractJobRequest createValDb = JobCreateStrategy.createJobRequest("createDb", null);
			createValDb.setAttrValue("serverType", DbServerType.TYPE_ORACLE);
			createValDb.setAttrValue("bizType", BizType.DB_COP_VERSION);
			createValDb.setAttrValue("descp", "validation temp db");
			subJobRequests.put("createValDb", createValDb);
			//expBatchDb
			AbstractJobRequest expValDb = JobCreateStrategy.createJobRequest("gdbExport", null);
			expValDb.setAttrValue("condition", "mesh");
			expValDb.setAttrValue("featureType", "all");
			expValDb.setAttrValue("dataIntegrity", true);
			subJobRequests.put("expValDb", expValDb);
		}
		//createBakDb
		AbstractJobRequest val = JobCreateStrategy.createJobRequest("checkCore", null);
		subJobRequests.put("val", val);
		
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
