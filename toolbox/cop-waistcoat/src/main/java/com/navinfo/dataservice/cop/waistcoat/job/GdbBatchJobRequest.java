package com.navinfo.dataservice.cop.waistcoat.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

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
	protected List<Integer> rules;
	protected int targetDbId;
	protected AbstractJobRequest createBatchDb;
	protected AbstractJobRequest expBatchDb;
	protected AbstractJobRequest createBakDb;
	protected AbstractJobRequest copyBakDb;
	protected AbstractJobRequest batchBody;
	protected AbstractJobRequest diffBody;
	protected AbstractJobRequest commitBody;
	
	@Override
	public int getStepCount() throws JobException {
		int count = 0;
		count+=createBatchDb.getStepCount();
		count+=expBatchDb.getStepCount();
		count+=createBakDb.getStepCount();
		count+=copyBakDb.getStepCount();
		count+=batchBody.getStepCount();
		count+=diffBody.getStepCount();
		count+=commitBody.getStepCount();
		return count;
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

	public List<Integer> getRules() {
		return rules;
	}

	public void setRules(List<Integer> rules) {
		this.rules = rules;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public AbstractJobRequest getCreateBatchDb() {
		return createBatchDb;
	}

	public void setCreateBatchDb(AbstractJobRequest createBatchDb) {
		this.createBatchDb = createBatchDb;
	}

	public AbstractJobRequest getExpBatchDb() {
		return expBatchDb;
	}

	public void setExpBatchDb(AbstractJobRequest expBatchDb) {
		this.expBatchDb = expBatchDb;
	}

	public AbstractJobRequest getCreateBakDb() {
		return createBakDb;
	}

	public void setCreateBakDb(AbstractJobRequest createBakDb) {
		this.createBakDb = createBakDb;
	}

	public AbstractJobRequest getCopyBakDb() {
		return copyBakDb;
	}

	public void setCopyBakDb(AbstractJobRequest copyBakDb) {
		this.copyBakDb = copyBakDb;
	}

	public AbstractJobRequest getBatchBody() {
		return batchBody;
	}

	public void setBatchBody(AbstractJobRequest batchBody) {
		this.batchBody = batchBody;
	}

	public AbstractJobRequest getDiffBody() {
		return diffBody;
	}

	public void setDiffBody(AbstractJobRequest diffBody) {
		this.diffBody = diffBody;
	}

	public AbstractJobRequest getCommitBody() {
		return commitBody;
	}

	public void setCommitBody(AbstractJobRequest commitBody) {
		this.commitBody = commitBody;
	}

}
