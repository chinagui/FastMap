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
		INFO_CONTENT	情报内容描述	VARCHAR2(200)		空	
		TASK_ID	任务id	NUMBER(10)	外键	空	
		INSERT_TIME	情报插入时间	TIMESTAMP			
	 */
	private String inforId ;
	private String inforName;
	private String geometry;
	private Integer inforLevel;
	private Integer planStatus;
	private String inforContent;
	private Integer taskId;
	private Timestamp insertTime;
	
	public Infor (){
	}
	
	public String getInforId() {
		return inforId;
	}
	public void setInforId(String inforId) {
		this.inforId = inforId;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Infor [inforId=" + inforId +",inforName="+inforName+",geometry="+geometry+",inforLevel="+inforLevel+",planStatus="+planStatus+",inforContent="+inforContent+",taskId="+taskId+",insertTime="+insertTime+"]";
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

	public String getInforContent() {
		return inforContent;
	}

	public void setInforContent(String inforContent) {
		this.inforContent = inforContent;
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
	
	
	
}
