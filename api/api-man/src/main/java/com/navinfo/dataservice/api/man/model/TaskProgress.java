package com.navinfo.dataservice.api.man.model;

import java.sql.Clob;
import java.sql.Timestamp;

public class TaskProgress extends BaseObj {
	public int getPhaseId() {
		return phaseId;
	}
	public int getTaskId() {
		return taskId;
	}
	public void setPhaseId(int phaseId) {
		if(this.checkValue("PHASE_ID",this.phaseId,phaseId)){this.phaseId = phaseId;}
		}
	public void setTaskId(int taskId) {
		if(this.checkValue("TASK_ID",this.taskId,taskId)){this.taskId = taskId;}
		}
	public int getPhase() {
		return phase;
	}
	public void setPhase(int phase) {
		if(this.checkValue("PHASE",this.phase,phase)){this.phase = phase;}
		}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		if(this.checkValue("STATUS",this.status,status)){this.status = status;}
		}
	public int getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(int createUserId) {
		if(this.checkValue("CREAT_USERID",this.createUserId,createUserId)){this.createUserId = createUserId;}
		}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		if(this.checkValue("MESSAGE",this.message,message)){this.message = message;}
		}
	public Long getOperator() {
		return operator;
	}
	public void setOperator(Long operator) {
		if(this.checkValue("OPERATOR",this.operator,operator)){this.operator = operator;}
		}
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		if(this.checkValue("PARAMETER",this.parameter,parameter)){this.parameter = parameter;}
		}
	public Timestamp getCreatDate() {
		return creatDate;
	}
	public void setCreatDate(Timestamp creatDate) {
		if(this.checkValue("CREATE_DATE",this.creatDate,creatDate)){this.creatDate = creatDate;}
		}
	public Timestamp getStartDate() {
		return startDate;
	}
	public void setStartDate(Timestamp startDate) {
		if(this.checkValue("START_DATE",this.startDate,startDate)){this.startDate = startDate;}
	}
	public Timestamp getEndDate() {
		return endDate;
	}
	public void setEndDate(Timestamp endDate) {
		if(this.checkValue("END_DATE",this.endDate,endDate)){this.endDate = endDate;}
	}
	private int taskId;
	private int phase;
	private int status;
	private int phaseId;
	private int createUserId;
	private String message;
	private Long operator;
	private String parameter;
	private Timestamp creatDate;
	private Timestamp startDate;
	private Timestamp endDate;
}