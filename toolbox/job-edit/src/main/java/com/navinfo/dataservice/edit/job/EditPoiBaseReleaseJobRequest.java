package com.navinfo.dataservice.edit.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

public class EditPoiBaseReleaseJobRequest extends AbstractJobRequest {
	private List<Integer> gridIds;
	private int targetDbId;
	private List<String> checkRules;
	private List<String> batchRules;


	@Override
	public String getJobType() {
		return "editPoiBaseRelease";
	}
	@Override
	public String getJobTypeName(){
		return "POI行编提交";
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub
		
	}

	public List<Integer> getGridIds() {
		return gridIds;
	}

	public void setGridIds(List<Integer> gridIds) {
		this.gridIds = gridIds;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public List<String> getCheckRules() {
		return checkRules;
	}

	public void setCheckRules(List<String> checkRules) {
		this.checkRules = checkRules;
	}

	public List<String> getBatchRules() {
		return batchRules;
	}

	public void setBatchRules(List<String> batchRules) {
		this.batchRules = batchRules;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		subJobRequests = new HashMap<String,AbstractJobRequest>();
		//validation
//		List<String> checkRule=new ArrayList<String>();
//		String defaultStr = "GLM60041,GLM60236,GLM60103,GLM60280,GLM60279,GLM60243,GLM60021,GLM60138,CHR63095,CHR73001,COM60038,COM60041,COM60245,GLM60154,GLM60249,GLM60257,GLM60255,GLM60303,GLM60029,GLM60143,GLM60064,GLM60078,GLM60074,GLM60282,GLM60321,GLM60063,GLM60037,GLM60066,GLM60211,GLM60247,GLM60248,GLM60069,GLM60213,GLM60079,GLM60245,GLM60065,GLM60271,GLM60322,GLM60169,GLM60254,GLM60323,GLM60274,GLM60273,GLM60275,GLM60276,GLM60250,GLM60053,GLM60304,GLM60023,GLM60335,CHR73003,CHR73002,GLM60014,GLM60191,GLM60189,GLM60174,GLM60201,GLM60173,GLM60177,GLM60178,GLM60327,GLM60172,GLM60332,GLM60333,GLM60334,GLM60305,GLM60306,GLM60314,GLM60996,GLM60190,GLM60181,GLM60302,GLM60222,GLM60224,GLM60225,GLM60994,GLM60406,GLM60407,GLM60376,GLM60377,GLM60378,GLM60228,GLM60227,GLM60226,GLM60080,GLM60342,GLM60095,GLM60350,GLM60351,GLM60353,GLM60358,CHR71011,CHR63040,CHR63041,CHR63042,CHR63043,CHR63044,CHR63045,CHR63046,CHR63047,CHR63048,CHR63049,CHR63050,CHR63051,CHR63052,CHR63053,CHR63054,CHR63055,CHR63056,CHR63057,CHR63058,CHR63059,CHR63060,CHR63061,CHR63062,CHR63063,CHR63064,CHR63065,CHR63066,CHR63067,CHR63068,CHR63069,CHR63070,CHR63071,CHR63072,CHR63073,CHR63074,CHR63075,CHR63076,CHR70004,CHR70005,CHR70006,CHR70007,CHR70008,CHR70009,CHR70010,CHR70011,CHR70012,CHR70013,CHR70014,CHR70015,CHR70016,CHR70017,CHR70018,CHR70019,CHR70020,CHR70021,CHR70022,CHR70023,CHR70024,CHR71003,CHR71004,CHR71005,CHR71006,CHR71007,CHR71008,CHR71009,CHR71010,CHR71012,CHR71013,CHR71016,CHR71017,CHR71018,CHR71019,CHR71020,CHR71022,CHR71023,CHR71040,CHR71042,CHR71043,CHR71051,CHR71054,CHR71055,CHR71056,CHR71057,CHR71058,CHR71059,CHR71060,CHR71061,CHR71062,CHR71063,CHR71064,CHR71065,CHR71066,CHR71067,CHR71068,CHR72002,CHR72003,CHR72004,CHR72005,CHR72006,CHR72009,CHR72020,CHR72021,CHR72022,CHR72023,CHR72024,CHR72025,CHR72026,CHR72027,CHR72028,CHR72029,CHR72030,CHR72031,CHR72032,CHR72033,CHR72034,CHR72035,CHR72036,CHR72037,CHR72038,CHR72039,CHR72040,CHR72041,CHR72042,CHR72043,GLM60194,GLM60424,GLM60089,GLM60142";
//		String rulestr = SystemConfigFactory.getSystemConfig().getValue("poi.cop.check.rules",defaultStr);
//		for(String r:rulestr.split(",")){
//			checkRule.add(r);
//		}
//		checkRule.add("GLM01004");
//		this.setCheckRules(checkRule);
		AbstractJobRequest validation = JobCreateStrategy.createJobRequest("gdbValidation", null);
		validation.setAttrValue("grids", gridIds);
		validation.setAttrValue("rules", JSONArray.fromObject(this.checkRules));
		validation.setAttrValue("targetDbId", targetDbId);
		validation.setAttrValue("timeOut", 300);
		subJobRequests.put("validation", validation);
		//batch
		//不做批处理
//		String batchStr = SystemConfigFactory.getSystemConfig().getValue("poi.cop.batch.rules");
//		List<String> batchRule=new ArrayList<String>();
//		for(String r:batchStr.split(",")){
//			batchRule.add(r);
//		}
//		batchRule.add("BATCH_TUNNELNAME");
//		batchRule.add("BATCH_SLE");
//		batchRule.add("BATCH_VECTOR_ATTR");
//		batchRule.add("BATCH_LANE_PARKING_FLAG");
//		batchRule.add("MILEAGEPILE_NAMECODE");
		
//		this.setBatchRules(batchRule);
//		AbstractJobRequest batch = JobCreateStrategy.createJobRequest("gdbBatch", null);
//		batch.setAttrValue("grids", gridIds);
//		batch.setAttrValue("rules", JSONArray.fromObject(this.batchRules));
//		batch.setAttrValue("targetDbId", targetDbId);
//		subJobRequests.put("batch", batch);
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
