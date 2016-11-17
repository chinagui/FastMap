package com.navinfo.dataservice.engine.editplus.model;

import com.vividsolutions.jts.geom.Geometry;

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
		this.pid = pid;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public double getxGuide() {
		return xGuide;
	}
	public void setxGuide(double xGuide) {
		this.xGuide = xGuide;
	}
	public double getyGuide() {
		return yGuide;
	}
	public void setyGuide(double yGuide) {
		this.yGuide = yGuide;
	}
	public long getLinkPid() {
		return linkPid;
	}
	public void setLinkPid(long linkPid) {
		this.linkPid = linkPid;
	}
	public int getMeshId() {
		return meshId;
	}
	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

}
