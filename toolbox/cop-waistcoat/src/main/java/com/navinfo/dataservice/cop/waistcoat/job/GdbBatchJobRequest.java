package com.navinfo.dataservice.cop.waistcoat.job;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.api.datahub.model.BizType;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/** 
* @ClassName: GdbBatchJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月17日 下午6:02:27 
* @Description: TODO
*  
*/
public class GdbBatchJobRequest extends AbstractJobRequest {
	protected List<Integer> grids;
	protected List<String> rules;
	protected int extendCount=0;
	protected int targetDbId;//批处理的导出源库
	protected int batchDbId=0;//如果存在可用的子版本库，可以直接使用，不用再创建
	protected String pidDbInfo;
	protected boolean reuseDb=true;

	@Override
	protected int myStepCount() throws JobException {
		return 0;//什么大事都没做，全调子job
	}

	@Override
	public void defineSubJobRequests()throws JobCreateException{
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		//createBatchDb
		if(batchDbId==0){
			AbstractJobRequest createBatchDb = JobCreateStrategy.createJobRequest("createDb", null);
			createBatchDb.setAttrValue("serverType", DbServerType.TYPE_ORACLE);
			createBatchDb.setAttrValue("bizType", BizType.DB_COP_VERSION);
			createBatchDb.setAttrValue("descp", "batch temp db");
			subJobRequests.put("createBatchDb", createBatchDb);
			//expBatchDb
			AbstractJobRequest expBatchDb = JobCreateStrategy.createJobRequest("gdbExport", null);
			expBatchDb.setAttrValue("condition", "mesh");
			expBatchDb.setAttrValue("featureType", "all");
			expBatchDb.setAttrValue("dataIntegrity", true);
			subJobRequests.put("expBatchDb", expBatchDb);
		}
		//createBakDb
		AbstractJobRequest createBakDb = JobCreateStrategy.createJobRequest("createDb", null);
		createBakDb.setAttrValue("serverType", DbServerType.TYPE_ORACLE);
		createBakDb.setAttrValue("bizType", BizType.DB_COP_VERSION);
		createBakDb.setAttrValue("descp", "batch bak db");
		subJobRequests.put("createBakDb", createBakDb);
		//copyBakDb
		AbstractJobRequest copyBakDb = JobCreateStrategy.createJobRequest("gdbFullCopy", null);
		copyBakDb.setAttrValue("featureType", "all");
		subJobRequests.put("copyBakDb", copyBakDb);
		//batch
		AbstractJobRequest batch = JobCreateStrategy.createJobRequest("batchCore", null);
		subJobRequests.put("batch", batch);
		//diff
		AbstractJobRequest diff = JobCreateStrategy.createJobRequest("diff", null);
		subJobRequests.put("diff", diff);
		//commit
		AbstractJobRequest commit = JobCreateStrategy.createJobRequest("gdbImport", null);
		commit.setAttrValue("logMoveType", "copBatch");
		subJobRequests.put("commit", commit);
		
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest#validate()
	 */
	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

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

	public int getExtendCount() {
		return extendCount;
	}

	public void setExtendCount(int extendCount) {
		this.extendCount = extendCount;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public int getBatchDbId() {
		return batchDbId;
	}

	public void setBatchDbId(int batchDbId) {
		this.batchDbId = batchDbId;
	}

	public String getPidDbInfo() {
		return pidDbInfo;
	}

	public void setPidDbInfo(String pidDbInfo) {
		this.pidDbInfo = pidDbInfo;
	}

	public boolean isReuseDb() {
		return reuseDb;
	}

	public void setReuseDb(boolean reuseDb) {
		this.reuseDb = reuseDb;
	}

	@Override
	public String getJobType() {
		return "gdbBatch";
	}

	@Override
	public String getJobTypeName(){
		return "批处理";
	}

}
