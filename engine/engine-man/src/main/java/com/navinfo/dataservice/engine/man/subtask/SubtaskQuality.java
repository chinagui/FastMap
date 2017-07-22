package com.navinfo.dataservice.engine.man.subtask;

import com.navinfo.dataservice.api.man.model.BaseObj;
import com.vividsolutions.jts.geom.Geometry;

public class SubtaskQuality extends BaseObj {
	private int qualityId;
	private int subtaskId;
	private Geometry geometry;
	
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		if(this.checkValue("GEOMETRY",this.geometry,geometry)){this.geometry = geometry;}
	}
	public int getQualityId() {
		return qualityId;
	}
	public void setQualityId(int qualityId) {
		if(this.checkValue("QUALITY_ID",this.qualityId,qualityId)){this.qualityId = qualityId;}
	}
	public int getSubtaskId() {
		return subtaskId;
	}
	public void setSubtaskId(int subtaskId) {
		if(this.checkValue("SUBTASK_ID",this.subtaskId,subtaskId)){this.subtaskId = subtaskId;}
	}
}
