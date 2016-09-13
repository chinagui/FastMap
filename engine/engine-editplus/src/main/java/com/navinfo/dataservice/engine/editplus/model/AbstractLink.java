package com.navinfo.dataservice.engine.editplus.model;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: AbstractLink
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: AbstractLink.java
 */
public abstract class AbstractLink extends BasicObj {
	protected long sNodePid;
	protected long eNodePid;
	protected Geometry geometry;
	protected double length;
	public long getsNodePid() {
		return sNodePid;
	}
	public void setsNodePid(long sNodePid) {
		this.sNodePid = sNodePid;
	}
	public long geteNodePid() {
		return eNodePid;
	}
	public void seteNodePid(long eNodePid) {
		this.eNodePid = eNodePid;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	
	public AbstractLink copyLink(){
		return null;
	}

}
