package com.navinfo.dataservice.api.man.model.subtask;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: SubtaskListByUser
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskListByUser.java
 */
public class SubtaskListByUser implements Serializable {

	/**
	 * 
	 */
	public SubtaskListByUser() {
		// TODO Auto-generated constructor stub
	}
	
	public SubtaskListByUser(int subtaskId,String name,int stage,int type,int status,String descp,int dbId,List<Integer> gridIds
			,String geometry,Timestamp planStartDate,Timestamp planEndDate) {
		// TODO Auto-generated constructor stub
		this.subtaskId = subtaskId;
		this.name = name;
		this.stage = stage;
		this.type = type;
		this.status = status;
		this.descp = descp;
		this.dbId = dbId;
		this.gridIds = gridIds;
		this.geometry = geometry;
		this.planStartDate = planStartDate;
		this.planEndDate = planEndDate;
		
	}
	
	@ApiModelProperty(position = 1, required = true, value = "子任务id")
	private Integer subtaskId ;
	@ApiModelProperty(position = 1, required = true, value = "子任务名称")
	private String name ;
	@ApiModelProperty(position = 1, required = true, value = "子任务阶段")
	private Integer stage ;
	@ApiModelProperty(position = 1, required = true, value = "子任务类型")
	private Integer type ;
	@ApiModelProperty(position = 1, required = true, value = "子任务状态，1, 开启/ 0 关闭")
	private Integer status ;
	@ApiModelProperty(position = 1, required = true, value = "子任务描述")
	private String descp ;
	@ApiModelProperty(position = 1, required = true, value = "子任务所属大区库的id")
	private Integer dbId ;
	@ApiModelProperty(position = 1, required = true, value = "计划开始时间")
	private Timestamp planStartDate ;
	@ApiModelProperty(position = 1, required = true, value = "计划结束时间")
	private Timestamp planEndDate;

	@ApiModelProperty(position = 1, required = false, value = "子任务几何")
	private String geometry ;
	@ApiModelProperty(position = 1, required = false, value = "子任务gridId列表")
	private List<Integer> gridIds;
	
	public String getName(){
		if(null==name){return "";}
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Integer> getGridIds(){
		return gridIds;
	}
	public void setGridIds(List<Integer> list) {
		this.gridIds = list;
	}
	public Integer getSubtaskId() {
		if(null==subtaskId){return 0;}
		return subtaskId;
	}
	public void setSubtaskId(Integer subtaskId) {
		this.subtaskId = subtaskId;
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
	public String getGeometry() {
		if(null==geometry){return "";}
		return geometry;
	}
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
	public Timestamp getPlanStartDate() {
		return planStartDate;
	}
	public void setPlanStartDate(Timestamp planStartDate) {
		this.planStartDate = planStartDate;
	}
	public Timestamp getPlanEndDate() {
		return planEndDate;
	}
	public void setPlanEndDate(Timestamp planEndDate) {
		this.planEndDate = planEndDate;
	}
	public int getDbId(){
		return dbId;
	}
	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

}
