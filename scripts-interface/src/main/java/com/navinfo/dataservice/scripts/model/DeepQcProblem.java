package com.navinfo.dataservice.scripts.model;

import java.util.Date;

public class DeepQcProblem {
	
	private int id;
	private int subtaskId;
	private String subtaskName;
	private String workObject;
	private String poiNum;
	private String workItem;
	private String workItemType;
	private String detailField;
	private String oldValue;
	private String errorType;
	private String errorLevel;
	private String problemDesc;
	private String newValue;
	private String worker;
	private String subtaskGroup;
	private Date workTime;
	private String qcWorker;
	private Date qcTime;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	public int getSubtaskId() {
		return subtaskId;
	}

	public void setSubtaskId(int subtaskId) {
		this.subtaskId = subtaskId;
	}

	public String getSubtaskName() {
		return subtaskName;
	}

	public void setSubtaskName(String subtaskName) {
		this.subtaskName = subtaskName;
	}

	public String getWorkObject() {
		return workObject;
	}

	public void setWorkObject(String workObject) {
		this.workObject = workObject;
	}


	public String getPoiNum() {
		return poiNum;
	}

	public void setPoiNum(String poiNum) {
		this.poiNum = poiNum;
	}

	public String getWorkItem() {
		return workItem;
	}

	public void setWorkItem(String workItem) {
		this.workItem = workItem;
	}

	public String getWorkItemType() {
		return workItemType;
	}

	public void setWorkItemType(String workItemType) {
		this.workItemType = workItemType;
	}

	public String getDetailField() {
		return detailField;
	}

	public void setDetailField(String detailField) {
		this.detailField = detailField;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public String getErrorLevel() {
		return errorLevel;
	}

	public void setErrorLevel(String errorLevel) {
		this.errorLevel = errorLevel;
	}

	public String getProblemDesc() {
		return problemDesc;
	}

	public void setProblemDesc(String problemDesc) {
		this.problemDesc = problemDesc;
	}

	public Date getWorkTime() {
		return workTime;
	}

	public void setWorkTime(Date workTime) {
		this.workTime = workTime;
	}

	public Date getQcTime() {
		return qcTime;
	}

	public void setQcTime(Date qcTime) {
		this.qcTime = qcTime;
	}

	public String getSubtaskGroup() {
		return subtaskGroup;
	}

	public void setSubtaskGroup(String subtaskGroup) {
		this.subtaskGroup = subtaskGroup;
	}

	public String getWorker() {
		return worker;
	}

	public void setWorker(String worker) {
		this.worker = worker;
	}

	public String getQcWorker() {
		return qcWorker;
	}

	public void setQcWorker(String qcWorker) {
		this.qcWorker = qcWorker;
	}
	
	

}
