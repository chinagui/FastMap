package com.navinfo.dataservice.cop.waistcoat.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: GdbValidationJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月21日 下午3:52:48 
* @Description: TODO
*  
*/
public class GdbValidationJobRequest extends AbstractJobRequest {
	protected List<Integer> grids;
	protected List<Integer> rules;
	protected int targetDbId;
	protected int valDbId=0;
	protected AbstractJobRequest createValDb;
	protected AbstractJobRequest expValDb;
	protected AbstractJobRequest validation;
	
	@Override
	public String getJobType() {
		return "gdbValidation";
	}

	@Override
	public int getStepCount() throws JobException {
		int count =1;
		if(createValDb!=null&&expValDb!=null){
			count+=createValDb.getStepCount();
			count+=expValDb.getStepCount();
		}
		count+=validation.getStepCount();
		return count;
	}

	@Override
	public void validate() throws JobException {
		if(valDbId<1&&(createValDb==null||expValDb==null)){
			throw new JobException("检查的子版本库为指定，且未指定新创建方式。");
		}
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

	public int getValDbId() {
		return valDbId;
	}

	public void setValDbId(int valDbId) {
		this.valDbId = valDbId;
	}

	public AbstractJobRequest getCreateValDb() {
		return createValDb;
	}

	public void setCreateValDb(AbstractJobRequest createValDb) {
		this.createValDb = createValDb;
	}

	public AbstractJobRequest getExpValDb() {
		return expValDb;
	}

	public void setExpValDb(AbstractJobRequest expValDb) {
		this.expValDb = expValDb;
	}

}
