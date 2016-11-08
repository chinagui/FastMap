package com.navinfo.dataservice.engine.editplus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @ClassName: AbstractIx
 * 暂时用于IX_POI
 * @author xiaoxiaowen4127
 * @date 2016年11月4日
 * @Description: AbstractIx.java
 */
public abstract class AbstractIx extends BasicObj {
	
	protected Geometry geometry;
	protected double xGuide=0;
	protected double yGuide=0;
	protected long linkPid=0;
	protected long nameGroupid=0;
	protected int meshId;
	
	
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

}
