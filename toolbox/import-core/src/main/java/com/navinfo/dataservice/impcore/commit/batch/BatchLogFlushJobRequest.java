package com.navinfo.dataservice.impcore.commit.batch;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author MaYunFei
 * 2016年6月21日
 * 描述：import-coreBatchLogFlushRequest.java
 */
public class BatchLogFlushJobRequest extends AbstractJobRequest {
	private int batchDbId;
	private int targetDbId;
	private List<Integer> grids;
	public int getBatchDbId() {
		return batchDbId;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public List<Integer> getGrids() {
		return grids;
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

}

