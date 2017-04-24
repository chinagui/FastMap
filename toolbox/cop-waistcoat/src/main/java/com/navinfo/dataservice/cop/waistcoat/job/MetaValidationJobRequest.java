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
* @author zl
* @date 2017年4月16日  
* @Description: TODO
*  
*/
public class MetaValidationJobRequest extends AbstractJobRequest {
	protected String name;
	protected String nameGroupid;
	protected String adminId;
	protected String roadTypes;
	protected List<String> rules;
	protected List<Integer> nameIds;
	

	//	protected int targetDbId;
	protected int valDbId=0;
	protected int timeOut;
	
	@Override
	public String getJobType() {
		return "metaValidation";
	}

	@Override
	public String getJobTypeName(){
		return "道路名检查";
	}

	@Override
	public void validate() throws JobException {
		/*if(valDbId<1&&(this.getSubJobRequest("createValDb")==null||this.getSubJobRequest("expValDb")==null)){
			throw new JobException("检查的子版本库未指定，且未指定新创建方式。");
		}*/
	}


	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

//	public int getTargetDbId() {
//		return targetDbId;
//	}
//
//	public void setTargetDbId(int targetDbId) {
//		this.targetDbId = targetDbId;
//	}

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


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(String nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public String getRoadTypes() {
		return roadTypes;
	}

	public void setRoadTypes(String roadTypes) {
		this.roadTypes = roadTypes;
	}
	
	public List<Integer> getNameIds() {
		return nameIds;
	}

	public void setNameIds(List<Integer> nameIds) {
		this.nameIds = nameIds;
	}
	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		//createBatchDb
		AbstractJobRequest createValDb = JobCreateStrategy.createJobRequest("createDb", null);
		createValDb.setAttrValue("serverType", DbServerType.TYPE_ORACLE);
		createValDb.setAttrValue("bizType", BizType.DB_COP_VERSION);
		createValDb.setAttrValue("descp", "validation temp db");
		createValDb.setAttrValue("gdbVersion", "");
		subJobRequests.put("createValDb", createValDb);
		
		//expBatchDb
		/*AbstractJobRequest expValDb = JobCreateStrategy.createJobRequest("metaExport", null);
		expValDb.setAttrValue("condition", "mesh");
		expValDb.setAttrValue("featureType", "all");
		expValDb.setAttrValue("dataIntegrity", true);
		subJobRequests.put("expValDb", expValDb);*/
		
		//createBakDb
		AbstractJobRequest val = JobCreateStrategy.createJobRequest("checkCore", null);
		subJobRequests.put("val", val);
		
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
