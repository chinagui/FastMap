package com.navinfo.dataservice.column.job;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import net.sf.json.JSONObject;

public class Day2MonthPoiMergeJobRequest extends AbstractJobRequest {
	private String cityId;//需要日落月的城市id，为空表示全部DAY2MONTH_CONFIG中处于打开状态的城市
	private List<Integer> specRegionId;//需要日落月的大区id,为空表示全部大区都要落
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
		return "day2MonSync";
	}

	@Override
	public String getJobTypeName() {
		return "日落月";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 0;
	}

	@Override
	public void validate() throws JobException {

	}
	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public List<Integer> getSpecRegionId() {
		return specRegionId;
	}

	public void setSpecRegionId(List<Integer> specRegionId) {
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
