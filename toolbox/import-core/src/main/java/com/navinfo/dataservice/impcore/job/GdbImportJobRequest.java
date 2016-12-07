package com.navinfo.dataservice.impcore.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
* @ClassName: GdbImportJobRequest 
* @author Xiao Xiaowen 
* @date 2016年6月23日 上午11:25:27 
* @Description: TODO
*  
*/
public class GdbImportJobRequest extends AbstractJobRequest {

	protected String impType="default";//default,fullAndNonLock
	protected int logDbId;
	protected int targetDbId;
	protected List<Integer> grids;
	protected String stopTime;
	protected boolean ignoreError;
	protected String logMoveType="default";//如果是copBatch
	
	
	@Override
	public String getJobType() {
		return "gdbImport";
	}

	@Override
	public String getJobTypeName(){
		return "履历回库";
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

	public String getImpType() {
		return impType;
	}

	public void setImpType(String impType) {
		this.impType = impType;
	}

	public int getLogDbId() {
		return logDbId;
	}

	public void setLogDbId(int logDbId) {
		this.logDbId = logDbId;
	}

	public int getTargetDbId() {
		return targetDbId;
	}

	public void setTargetDbId(int targetDbId) {
		this.targetDbId = targetDbId;
	}

	public List<Integer> getGrids() {
		return grids;
	}

	public void setGrids(List<Integer> grids) {
		this.grids = grids;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}

	public boolean isIgnoreError() {
		return ignoreError;
	}

	public void setIgnoreError(boolean ignoreError) {
		this.ignoreError = ignoreError;
	}

	public String getLogMoveType() {
		return logMoveType;
	}

	public void setLogMoveType(String logMoveType) {
		this.logMoveType = logMoveType;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

}
