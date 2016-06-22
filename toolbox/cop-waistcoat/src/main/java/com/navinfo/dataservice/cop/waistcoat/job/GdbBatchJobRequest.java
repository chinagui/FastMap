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
	protected AbstractJobRequest batch;
	protected AbstractJobRequest diff;
	protected AbstractJobRequest commit;
	
	@Override
	public int getStepCount() throws JobException {
		int count = 1;
		count+=createBatchDb.getStepCount();
		count+=expBatchDb.getStepCount();
		count+=createBakDb.getStepCount();
		count+=copyBakDb.getStepCount();
		count+=batch.getStepCount();
		count+=diff.getStepCount();
		count+=commit.getStepCount();
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

	public AbstractJobRequest getBatch() {
		return batch;
	}

	public void setBatch(AbstractJobRequest batch) {
		this.batch = batch;
	}

	public AbstractJobRequest getDiff() {
		return diff;
	}

	public void setDiff(AbstractJobRequest diff) {
		this.diff = diff;
	}

	public AbstractJobRequest getCommit() {
		return commit;
	}

	public void setCommit(AbstractJobRequest commit) {
		this.commit = commit;
	}

	@Override
	public String getJobType() {
		return "gdbBatch";
	}

}
