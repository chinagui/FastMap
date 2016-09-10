package com.navinfo.dataservice.edit.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;

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
		List<String> checkRule=new ArrayList<String>();
		String rulestr = "GLM80001,GLM80002,GLM80003,GLM80004,GLM80005,GLM80006,GLM80007,GLM80013,GLM80015,GLM80016,GLM80017,GLM80025,GLM80024,GLM80028,GLM80027,GLM80018,GLM80026,GLM80019,GLM80020,GLM80021,GLM80029,GLM29162,GLM29163,GLM29003,GLM01433,GLM29005,GLM29006,GLM29007,GLM29008,GLM29010,GLM29011,GLM29012,GLM29013,GLM29014,GLM29016,GLM29017,GLM29018,GLM29019,GLM29020,GLM29021,GLM29022,GLM29023,GLM29025,GLM29026,GLM29028,GLM29029,GLM29030,GLM29032,GLM29033,GLM29040,GLM29041,GLM29042,GLM29043,GLM29044,GLM29045,GLM29047,GLM29048,GLM29049,GLM29050,GLM29052,GLM29054,GLM29055,GLM29056,GLM29057,GLM29058,GLM29059,GLM29060,GLM29061,GLM29062,GLM29063,GLM29064,GLM29078,GLM29079,GLM29082,GLM29083,GLM29084,GLM29085,GLM29086,GLM29087,GLM29088,GLM29089,GLM29090,GLM29091,GLM29092,GLM29093,GLM29094,GLM29095,GLM29097,GLM29098,GLM29099,GLM29100,GLM29102,GLM29103,GLM29104,GLM29105,GLM29106,GLM29107,GLM29109,GLM29110,GLM29114,GLM29115,GLM29111,GLM29112,GLM29113,GLM29144,GLM29116,GLM29171,GLM29172,GLM32102,GLM32106,GLM32108,GLM32109,GLM32110,GLM32095,GLM32096,GLM32104,GLM32103,GLM32080,COM20206,COM20213,COM20218,COM20224,COM20232,COM20268,COM20409,COM20414,COM20484,COM20709,COM400106,COM400107,COM400108,COM400109,COM400110,COM400111,COM400112,COM400113,COM400114,COM400115,COM60286,COM60287,COM60288,GLM32072,GLM32073,GLM32074,GLM32043,GLM32049,GLM32053,GLM32061,GLM32071,GLM32111,GLM32113,GLM32065,GLM32066,GLM32069,GLM32070,GLM32092,GLM32093,GLM32094,GLM32098,GLM32076,GLM32116,GLM32117,GLM32119,GLM32075,GLM32114,GLM32115,GLM32122,GLM32123,GLM01442,GLM29108,GLM02005,GLM02007,GLM02039,GLM03026,GLM01443,GLM01447,GLM01448,GLM53079,GLM53077,GLM53052,GLM53051,GLM53078,GLM01547,GLM53075,GLM53050,GLM53076,GLM53056,GLM53055,GLM53054,GLM53071,GLM53053,GLM53090,GLM53072,GLM53059,GLM53070,GLM53058,GLM53057,GLM53068,GLM53086,GLM53069,GLM53088,GLM53064,GLM53065,GLM53066,GLM53067,GLM53060,GLM53061,GLM53062,GLM53080,GLM53063,GLM53081,GLM53084,GLM53085,GLM53030,GLM53038,GLM53004,GLM53037,GLM53005,GLM53002,GLM53034,GLM53001,GLM53031,GLM53009,GLM53008,GLM53007,GLM53039,GLM53006,GLM53089,GLM53040,GLM53043,GLM53024,GLM53014,GLM53027,GLM53045,GLM53026,GLM53016,GLM53044,GLM53047,GLM53021,GLM53020,GLM53046,GLM53010,GLM53049,GLM53011,GLM53022,GLM53012,GLM53048,GLM53029,GLM53019,GLM03097,GLM52026,GLM51037,GLM03070,GLM01157,GLM50023,GLM50022,GLM01155,GLM50021,GLM01156,GLM03069,GLM50020,GLM01159,GLM01150,GLM54030,GLM03068,GLM01154,GLM51070,GLM01151,GLM01152,GLM52011,GLM52010,GLM50019,GLM30014,GLM51063,GLM01144,GLM30018,GLM51062,GLM01145,GLM30017,GLM51065,GLM01146,GLM30016,GLM01147,GLM51067,GLM01148,GLM01149,GLM51069,GLM51068,GLM03073,GLM03072,GLM01140,GLM52018,GLM01142,GLM51060,GLM01143,GLM01160,GLM52042,GLM52041,GLM51019,GLM51018,GLM51017,GLM50005,GLM50006,GLM50042,GLM52040,GLM50082,GLM51059,GLM51058,GLM52008,GLM01139,GLM51057,GLM01137,GLM52005,GLM01138,GLM30006,GLM01135,GLM01136,GLM01133,GLM52009,GLM01134,GLM01131,GLM28029,GLM01132,GLM01130,GLM52050,GLM51005,GLM01126,GLM01127,GLM01128,GLM01129,GLM01122,GLM01123,GLM01125,GLM52039,GLM01025,GLM01120,GLM01121,GLM59035,GLM59028,GLM60326,GLM59026,GLM01422,GLM59027";
		for(String r:rulestr.split(",")){
			checkRule.add(r);
		}
//		checkRule.add("GLM01004");
		this.setCheckRules(checkRule);
		AbstractJobRequest validation = JobCreateStrategy.createJobRequest("gdbValidation", null);
		validation.setAttrValue("grids", gridIds);
		validation.setAttrValue("rules", JSONArray.fromObject(this.checkRules));
		validation.setAttrValue("targetDbId", targetDbId);
		validation.setAttrValue("timeOut", 300);
		subJobRequests.put("validation", validation);
		//batch
		List<String> batchRule=new ArrayList<String>();
//		batchRule.add("BATCH_TUNNELNAME");
//		batchRule.add("BATCH_SLE");
//		batchRule.add("BATCH_VECTOR_ATTR");
		batchRule.add("BATCH_LANE_PARKING_FLAG");
//		batchRule.add("MILEAGEPILE_NAMECODE");
		
		this.setBatchRules(batchRule);
		AbstractJobRequest batch = JobCreateStrategy.createJobRequest("gdbBatch", null);
		batch.setAttrValue("grids", gridIds);
		batch.setAttrValue("rules", JSONArray.fromObject(this.batchRules));
		batch.setAttrValue("targetDbId", targetDbId);
		subJobRequests.put("batch", batch);
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
