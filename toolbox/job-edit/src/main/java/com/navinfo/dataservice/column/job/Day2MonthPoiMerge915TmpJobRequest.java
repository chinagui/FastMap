package com.navinfo.dataservice.column.job;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import net.sf.json.JSONObject;

public class Day2MonthPoiMerge915TmpJobRequest extends AbstractJobRequest {
	private String tmpOpTable;//刷履历的临时表
	private String tempFailLogTable;//刷履历报错临时表 
	private int onlyFlushLog; 
	private Integer specRegionId;//需要日落月的大区id,为空表示全部大区都要落
	private List<Integer> specMeshes;//需要日落月的大区id,为空表示全部大区都要落
	private int phaseId;
	private int type;
	private int lot;
	private JSONObject taskInfo;//需要日落月的任务，以及大区库

	@Override
	public void defineSubJobRequests() throws JobCreateException {

	}

	@Override
	public String getJobType() {
		return "day2MonTempSync";
	}

	@Override
	public String getJobTypeName() {
		return "日落月Temp";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {

	}
	public String getTmpOpTable() {
		return tmpOpTable;
	}

	public void setTmpOpTable(String tmpOpTable) {
		this.tmpOpTable = tmpOpTable;
	}

	public String getTempFailLogTable() {
		return tempFailLogTable;
	}

	public void setTempFailLogTable(String tempFailLogTable) {
		this.tempFailLogTable = tempFailLogTable;
	}

	public int getOnlyFlushLog() {
		return onlyFlushLog;
	}

	public void setOnlyFlushLog(int onlyFlushLog) {
		this.onlyFlushLog = onlyFlushLog;
	}

	public Integer getSpecRegionId() {
		return specRegionId;
	}

	public void setSpecRegionId(Integer specRegionId) {
		this.specRegionId = specRegionId;
	}

	public List<Integer> getSpecMeshes() {
		return specMeshes;
	}

	public void setSpecMeshes(List<Integer> specMeshes) {
		this.specMeshes = specMeshes;
	}

	public int getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(int phaseId) {
		this.phaseId = phaseId;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public JSONObject getTaskInfo() {
		return taskInfo;
	}

	public void setTaskInfo(JSONObject taskInfo) {
		this.taskInfo = taskInfo;
	}

	public int getLot() {
		return lot;
	}

	public void setLot(int lot) {
		this.lot = lot;
	}
}
