package com.navinfo.dataservice.api.man.model.subtask;

import java.io.Serializable;
import java.util.List;

import com.wordnik.swagger.annotations.ApiModelProperty;


/** 
 * @ClassName: SubtaskResponse
 * @author songdongyan
 * @date 2016年8月2日
 * @Description: SubtaskResponse.java
 */
public class SubtaskListByWkt implements Serializable{

	/**
	 * 
	 */
	public SubtaskListByWkt() {
		// TODO Auto-generated constructor stub
	}
	
	public SubtaskListByWkt(int subtaskId,String name,int stage,int type,int status,String descp,List<Integer> gridIds,String geometry) {
		// TODO Auto-generated constructor stub
		this.subtaskId = subtaskId;
		this.name = name;
		this.stage = stage;
		this.type = type;
		this.status = status;
		this.descp = descp;
		this.gridIds = gridIds;
		this.geometry = geometry;
		
	}
	
	@ApiModelProperty(position = 1, required = true, value = "子任务id")
	private Integer subtaskId ;
	@ApiModelProperty(position = 1, required = true, value = "子任务name")
	private String name ;
	@ApiModelProperty(position = 1, required = true, value = "子任务stage")
	private Integer stage ;
	@ApiModelProperty(position = 1, required = true, value = "子任务type")
	private Integer type ;
	@ApiModelProperty(position = 1, required = true, value = "子任务status")
	private Integer status ;
	@ApiModelProperty(position = 1, required = true, value = "子任务descp")
	private String descp ;
	@ApiModelProperty(position = 1, required = true, value = "子任务gridIds")
	private List<Integer> gridIds;
	@ApiModelProperty(position = 1, required = false, value = "子任务几何")
	private String geometry ;
	@ApiModelProperty(position = 1, required = false, value = "子任务几何")
	private String referGeometry ;

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
	public void setReferGeometry(String geometry) {
		this.referGeometry = geometry;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Subtask ["
				+ "subtaskId=" + subtaskId
				+ ",name=" + name
				+",stage="+stage
				+",type="+type
				+",status="+status
				+ ",gridIds=" + gridIds
				+",descp="+descp+"]";
	}

}
