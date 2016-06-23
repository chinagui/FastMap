package com.navinfo.dataservice.edit.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: BorrowDataJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月22日 下午3:55:59 
* @Description: TODO
*  
*/
public class BorrowDataJobRequest extends AbstractJobRequest {
	protected List<String> meshes;
	protected int borrowInDbId;
	protected AbstractJobRequest borrow;
	

	@Override
	public String getJobType() {
		return "borrowData";
	}

	@Override
	public int getStepCount() throws JobException {
		int count = 0;
		count+=borrow.getStepCount();
		return count;
	}

	@Override
	public void validate() throws JobException {

	}

	public List<String> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<String> meshes) {
		this.meshes = meshes;
	}

	public int getBorrowInDbId() {
		return borrowInDbId;
	}

	public void setBorrowInDbId(int borrowInDbId) {
		this.borrowInDbId = borrowInDbId;
	}

	public AbstractJobRequest getBorrow() {
		return borrow;
	}

	public void setBorrow(AbstractJobRequest borrow) {
		this.borrow = borrow;
	}

}
