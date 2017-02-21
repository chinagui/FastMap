package com.navinfo.dataservice.api.man.model;

import java.util.Set;

public class TaskCmsProgress {
	public int getPhaseId() {
		return phaseId;
	}
	public void setPhaseId(int phaseId) {
		this.phaseId = phaseId;
	}
	public Set<Integer> getGridIds() {
		return gridIds;
	}
	public void setGridIds(Set<Integer> gridIds) {
		this.gridIds = gridIds;
	}
	public Set<Integer> getMeshIds() {
		return meshIds;
	}
	public void setMeshIds(Set<Integer> meshIds) {
		this.meshIds = meshIds;
	}
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public int getPhase() {
		return phase;
	}
	public void setPhase(int phase) {
		this.phase = phase;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	public String getUserNickName() {
		return userNickName;
	}
	public void setUserNickName(String userNickName) {
		this.userNickName = userNickName;
	}
	public int getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}
	private int taskId;
	private int phase;
	private int status;
	private int phaseId;
	private int regionId;
	private int createUserId;
	private String userNickName;
	private Set<Integer> gridIds;
	private Set<Integer> meshIds;
}
