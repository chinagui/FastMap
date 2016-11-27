package com.navinfo.dataservice.dao.plus.model.basic;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @ClassName: AbstractIx
 * 暂时用于IX_POI
 * @author xiaoxiaowen4127
 * @date 2016年11月4日
 * @Description: AbstractIx.java
 */
public abstract class AbstractIx extends BasicRow {
	
	public AbstractIx(long objPid) {
		super(objPid);
	}

	protected long pid;
	protected Geometry geometry;
	protected double xGuide=0;
	protected double yGuide=0;
	protected long linkPid=0;
	protected int meshId;
	
	
	public long getPid() {
		return pid;
	}
	public void setPid(long pid) {
		if(this.checkValue("PID",this.pid,pid)){
			this.pid = pid;
		}
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Point geometry) {
		if(this.checkValue("GEOMETRY",this.geometry,geometry)){
			this.geometry = geometry;
		}
	}
	public void setGeometry(Geometry geometry) {
		if(this.checkValue("GEOMETRY",this.geometry,geometry)){
			this.geometry = geometry;
		}
	}
	public void setGeometry(LineString geometry) {
		if(this.checkValue("GEOMETRY",this.geometry,geometry)){
			this.geometry = geometry;
		}
	}

	public double getXGuide() {
		return xGuide;
	}
	public void setXGuide(double xGuide) {
		if(this.checkValue("X_GUIDE",this.xGuide,xGuide)){
			this.xGuide = xGuide;
		}
	}
	public double getYGuide() {
		return yGuide;
	}
	public void setYGuide(double yGuide) {
		if(this.checkValue("Y_GUIDE",this.yGuide,yGuide)){
			this.yGuide = yGuide;
		}
	}
	public long getLinkPid() {
		return linkPid;
	}
	public void setLinkPid(long linkPid) {
		if(this.checkValue("LINK_PID",this.linkPid,linkPid)){
			this.linkPid = linkPid;
		}
	}
	public int getMeshId() {
		return meshId;
	}
	public void setMeshId(int meshId) {
		if(this.checkValue("MESH_ID",this.meshId,meshId)){
			this.meshId = meshId;
		}
	}

}
