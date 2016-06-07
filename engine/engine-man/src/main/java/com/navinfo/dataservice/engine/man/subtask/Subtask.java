package com.navinfo.dataservice.engine.man.subtask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  Subtask 
* @author code generator
* @date 2016-06-06 07:40:15 
* @Description: TODO
*/
public class Subtask  {
	private Integer subtaskId ;
	private Integer blockId ;
	private Integer taskId ;
	private Object geometry ;
	private Integer stage ;
	private Integer type ;
	private Integer createUserId ;
	private Object createDate ;
	private Integer exeUserId ;
	private Integer status ;
	private Object planStartDate ;
	private Object planEndDate ;
	private Object startDate ;
	private Object endDate ;
	private String descp ;
	
	public Subtask (){
	}
	
	public Subtask (Integer subtaskId ,Integer blockId,Integer taskId,Object geometry,Integer stage,Integer type,Integer createUserId,Object createDate,Integer exeUserId,Integer status,Object planStartDate,Object planEndDate,Object startDate,Object endDate,String descp){
		this.subtaskId=subtaskId ;
		this.blockId=blockId ;
		this.taskId=taskId ;
		this.geometry=geometry ;
		this.stage=stage ;
		this.type=type ;
		this.createUserId=createUserId ;
		this.createDate=createDate ;
		this.exeUserId=exeUserId ;
		this.status=status ;
		this.planStartDate=planStartDate ;
		this.planEndDate=planEndDate ;
		this.startDate=startDate ;
		this.endDate=endDate ;
		this.descp=descp ;
	}
	public Integer getSubtaskId() {
		return subtaskId;
	}
	public void setSubtaskId(Integer subtaskId) {
		this.subtaskId = subtaskId;
	}
	public Integer getBlockId() {
		return blockId;
	}
	public void setBlockId(Integer blockId) {
		this.blockId = blockId;
	}
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	public Object getGeometry() {
		return geometry;
	}
	public void setGeometry(Object geometry) {
		this.geometry = geometry;
	}
	public Integer getStage() {
		return stage;
	}
	public void setStage(Integer stage) {
		this.stage = stage;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(Integer createUserId) {
		this.createUserId = createUserId;
	}
	public Object getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Object createDate) {
		this.createDate = createDate;
	}
	public Integer getExeUserId() {
		return exeUserId;
	}
	public void setExeUserId(Integer exeUserId) {
		this.exeUserId = exeUserId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Object getPlanStartDate() {
		return planStartDate;
	}
	public void setPlanStartDate(Object planStartDate) {
		this.planStartDate = planStartDate;
	}
	public Object getPlanEndDate() {
		return planEndDate;
	}
	public void setPlanEndDate(Object planEndDate) {
		this.planEndDate = planEndDate;
	}
	public Object getStartDate() {
		return startDate;
	}
	public void setStartDate(Object startDate) {
		this.startDate = startDate;
	}
	public Object getEndDate() {
		return endDate;
	}
	public void setEndDate(Object endDate) {
		this.endDate = endDate;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Subtask [subtaskId=" + subtaskId +",blockId="+blockId+",taskId="+taskId+",geometry="+geometry+",stage="+stage+",type="+type+",createUserId="+createUserId+",createDate="+createDate+",exeUserId="+exeUserId+",status="+status+",planStartDate="+planStartDate+",planEndDate="+planEndDate+",startDate="+startDate+",endDate="+endDate+",descp="+descp+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subtaskId == null) ? 0 : subtaskId.hashCode());
		result = prime * result + ((blockId == null) ? 0 : blockId.hashCode());
		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
		result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((createUserId == null) ? 0 : createUserId.hashCode());
		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
		result = prime * result + ((exeUserId == null) ? 0 : exeUserId.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((planStartDate == null) ? 0 : planStartDate.hashCode());
		result = prime * result + ((planEndDate == null) ? 0 : planEndDate.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((descp == null) ? 0 : descp.hashCode());
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
		Subtask other = (Subtask) obj;
		if (subtaskId == null) {
			if (other.subtaskId != null)
				return false;
		} else if (!subtaskId.equals(other.subtaskId))
			return false;
		if (blockId == null) {
			if (other.blockId != null)
				return false;
		} else if (!blockId.equals(other.blockId))
			return false;
		if (taskId == null) {
			if (other.taskId != null)
				return false;
		} else if (!taskId.equals(other.taskId))
			return false;
		if (geometry == null) {
			if (other.geometry != null)
				return false;
		} else if (!geometry.equals(other.geometry))
			return false;
		if (stage == null) {
			if (other.stage != null)
				return false;
		} else if (!stage.equals(other.stage))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (createUserId == null) {
			if (other.createUserId != null)
				return false;
		} else if (!createUserId.equals(other.createUserId))
			return false;
		if (createDate == null) {
			if (other.createDate != null)
				return false;
		} else if (!createDate.equals(other.createDate))
			return false;
		if (exeUserId == null) {
			if (other.exeUserId != null)
				return false;
		} else if (!exeUserId.equals(other.exeUserId))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (planStartDate == null) {
			if (other.planStartDate != null)
				return false;
		} else if (!planStartDate.equals(other.planStartDate))
			return false;
		if (planEndDate == null) {
			if (other.planEndDate != null)
				return false;
		} else if (!planEndDate.equals(other.planEndDate))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (descp == null) {
			if (other.descp != null)
				return false;
		} else if (!descp.equals(other.descp))
			return false;
		return true;
	}
	
	
	
}
