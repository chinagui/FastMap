package com.navinfo.dataservice.scripts.model;

/**
 * @Title: EightTypesPoi
 * @Package: com.navinfo.dataservice.scripts.model
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月14日
 * @Version: V1.0
 */
public class EightTypesPoi {

	private Integer valExceptionId;// 从1开始，顺序递增，步长为1
	private Integer groupId;// 赋值0
	private Integer level;// 赋值0
	private Integer ruleId;// 赋值0
	
	private String situation;// NULL
	private String information;
	private String suggestion;// NULL
	private String location;// 显示坐标：POINT (120.31579 31.55737)
	private String targets;// 赋值POI.PID:[IX_POI,96706311]
	
	private Integer additionInfo;// 根据图幅号关联sc_partition_meshlist表中批次action
	private Integer scopeFlag;// 赋值1
	
	private String created;// 赋值当前时间
	private String updated;// 赋值当前时间
	private String meshId;// 赋值POI图幅号
	private String provinceName;// 赋值NULL
	
	private Long taskId;// 赋值外业子任务号
	private Integer qaStatus;// 赋值2
	
	private String worker;// 赋值NULL
	private String qaWorker;// 赋值NULL
	private String reserved;// 赋值NULL
	private String taskName;// 赋值NULL
	
	private Integer logType;// 赋值0

	public Integer getValExceptionId() {
		return valExceptionId;
	}

	public void setValExceptionId(Integer valExceptionId) {
		this.valExceptionId = valExceptionId;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getRuleId() {
		return ruleId;
	}

	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;
	}

	public String getSituation() {
		return situation;
	}

	public void setSituation(String situation) {
		this.situation = situation;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	public Integer getAdditionInfo() {
		return additionInfo;
	}

	public void setAdditionInfo(Integer additionInfo) {
		this.additionInfo = additionInfo;
	}

	public Integer getScopeFlag() {
		return scopeFlag;
	}

	public void setScopeFlag(Integer scopeFlag) {
		this.scopeFlag = scopeFlag;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String getMeshId() {
		return meshId;
	}

	public void setMeshId(String meshId) {
		this.meshId = meshId;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Integer getQaStatus() {
		return qaStatus;
	}

	public void setQaStatus(Integer qaStatus) {
		this.qaStatus = qaStatus;
	}

	public String getWorker() {
		return worker;
	}

	public void setWorker(String worker) {
		this.worker = worker;
	}

	public String getQaWorker() {
		return qaWorker;
	}

	public void setQaWorker(String qaWorker) {
		this.qaWorker = qaWorker;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Integer getLogType() {
		return logType;
	}

	public void setLogType(Integer logType) {
		this.logType = logType;
	}

}
