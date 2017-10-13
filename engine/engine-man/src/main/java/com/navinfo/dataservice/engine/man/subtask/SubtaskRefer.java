package com.navinfo.dataservice.engine.man.subtask;

import com.navinfo.dataservice.api.man.model.BaseObj;
import com.vividsolutions.jts.geom.Geometry;

public class SubtaskRefer extends BaseObj {
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		if(this.checkValue("ID",this.id,id)){this.id = id;}
	}
	public int getBlockId() {
		return blockId;
	}
	public void setBlockId(int blockId) {
		if(this.checkValue("BLOCK_ID",this.blockId,blockId)){this.blockId = blockId;}
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		if(this.checkValue("GEOMETRY",this.geometry,geometry)){this.geometry = geometry;}
	}
	private int blockId;
	private Geometry geometry;
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	private int taskId;
}
