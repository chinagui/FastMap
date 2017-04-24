package com.navinfo.dataservice.api.man.model;

import java.sql.Timestamp;

/** 
* @ClassName:  Infor
* @author code generator
* @date 2016-06-15 02:27:03 
* @Description: TODO
*/
public class Infor  {
	/*
	 * INFOR_ID	情报ID	VARCHAR2 (50)	主键	非空	
		INFOR_NAME	情报名称	VARCHAR2 (200)		空	
		GEOMETRY	几何	CLOB		空	
		INFOR_LEVEL	情报级别	NUMBER(1)		空	
		PLAN_STATUS	情报规划状态	NUMBER(1)	0未规划，1已规划，2已关闭	0	
		TASK_ID	任务id	NUMBER(10)	外键	空	
		INSERT_TIME	情报插入时间	TIMESTAMP			
	 */
	

	private Integer inforId ;
	private String inforName;
	private String geometry;
	private Integer inforLevel;
	private Integer planStatus;
	private Integer taskId;
	private Timestamp insertTime;
	
	private Integer feedbackType;
	private Integer featureKind;
	
	private String adminName;
	private String inforCode;
	
	private Timestamp publishDate;
	private Timestamp expectDate;
	private Timestamp newsDate;

	private String topicName;
	private String infoTypeName;
	
	private String method;
	private Integer roadLength;
	private Integer sourceCode;





	
	public Infor (){
	}
	
	public int getInforId() {
		return inforId;
	}
	public void setInforId(int inforId) {
		this.inforId = inforId;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Infor [inforId=" + inforId +",inforName="+inforName+",geometry="+geometry+",inforLevel="+inforLevel+",planStatus="+planStatus+",taskId="+taskId+",insertTime="+insertTime+"]";
	}


	public String getInforName() {
		return inforName;
	}

	public void setInforName(String inforName) {
		this.inforName = inforName;
	}

	public String getGeometry() {
		return geometry;
	}

	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}

	public Integer getInforLevel() {
		return inforLevel;
	}

	public void setInforLevel(Integer inforLevel) {
		this.inforLevel = inforLevel;
	}

	public Integer getPlanStatus() {
		return planStatus;
	}

	public void setPlanStatus(Integer planStatus) {
		this.planStatus = planStatus;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public Timestamp getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Timestamp insertTime) {
		this.insertTime = insertTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inforId == null) ? 0 : inforId.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Infor other = (Infor) obj;
		if (inforId == null) {
			if (other.inforId != null)
				return false;
		} else if (!inforId.equals(other.inforId))
			return false;
		return true;
	}

	/**
	 * @return the feedbackType
	 */
	public Integer getFeedbackType() {
		return feedbackType;
	}

	/**
	 * @param feedbackType the feedbackType to set
	 */
	public void setFeedbackType(Integer feedbackType) {
		this.feedbackType = feedbackType;
	}

	/**
	 * @return the featureKind
	 */
	public Integer getFeatureKind() {
		return featureKind;
	}

	/**
	 * @param featureKind the featureKind to set
	 */
	public void setFeatureKind(Integer featureKind) {
		this.featureKind = featureKind;
	}

	/**
	 * @return the adminName
	 */
	public String getAdminName() {
		return adminName;
	}

	/**
	 * @param adminName the adminName to set
	 */
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	/**
	 * @return the infoCode
	 */
	public String getInforCode() {
		return inforCode;
	}

	/**
	 * @param infoCode the infoCode to set
	 */
	public void setInfroCode(String inforCode) {
		this.inforCode = inforCode;
	}

	/**
	 * @return the publishDate
	 */
	public Timestamp getPublishDate() {
		return publishDate;
	}

	/**
	 * @param publishDate the publishDate to set
	 */
	public void setPublishDate(Timestamp publishDate) {
		this.publishDate = publishDate;
	}

	/**
	 * @return the expectDate
	 */
	public Timestamp getExpectDate() {
		return expectDate;
	}

	/**
	 * @param expectDate the expectDate to set
	 */
	public void setExpectDate(Timestamp expectDate) {
		this.expectDate = expectDate;
	}

	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * @param topicName the topicName to set
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * @return the infoTypeName
	 */
	public String getInfoTypeName() {
		return infoTypeName;
	}

	/**
	 * @param infoTypeName the infoTypeName to set
	 */
	public void setInfoTypeName(String infoTypeName) {
		this.infoTypeName = infoTypeName;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the roadLength
	 */
	public Integer getRoadLength() {
		return roadLength;
	}

	/**
	 * @param roadLength the roadLength to set
	 */
	public void setRoadLength(Integer roadLength) {
		this.roadLength = roadLength;
	}

	/**
	 * @return the sourceCode
	 */
	public Integer getSourceCode() {
		return sourceCode;
	}

	/**
	 * @param sourceCode the sourceCode to set
	 */
	public void setSourceCode(Integer sourceCode) {
		this.sourceCode = sourceCode;
	}

	/**
	 * @return the newsDate
	 */
	public Timestamp getNewsDate() {
		return newsDate;
	}

	/**
	 * @param newsDate the newsDate to set
	 */
	public void setNewsDate(Timestamp newsDate) {
		this.newsDate = newsDate;
	}
	
	
	
}
