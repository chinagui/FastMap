package com.navinfo.dataservice.dao.plus.model.basic;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @ClassName: AbstractIx 暂时用于IX_POI
 * @author xiaoxiaowen4127
 * @date 2016年11月4日
 * @Description: AbstractIx.java 适用于IX_POI,IX_POINTADDRESS
 * @author zhangpengpeng
 * @date 2017年9月22日
 * @Description: 将linkPid,get及set方法移至IxPoi中，点门牌IxPointaddress也继承此类
 */
public abstract class AbstractIx extends BasicRow {

	public AbstractIx(long objPid) {
		super(objPid);
	}

	protected long pid;
	protected Geometry geometry;
	protected double xGuide = 0;
	protected double yGuide = 0;
	protected int meshId;

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		if (this.checkValue("PID", this.pid, pid)) {
			this.pid = pid;
		}
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Point geometry) {
		if (geometry != null) {
			geometry = (Point) GeoTranslator.transform(geometry, 1, 5);
		}
		if (this.checkValue("GEOMETRY", this.geometry, geometry)) {
			this.geometry = geometry;
		}
	}

	public void setGeometry(Geometry geometry) {
		if (geometry != null) {
			geometry = (Point) GeoTranslator.transform(geometry, 1, 5);
		}
		if (this.checkValue("GEOMETRY", this.geometry, geometry)) {
			this.geometry = geometry;
		}
	}

	public void setGeometry(LineString geometry) {
		if (geometry != null) {
			geometry = (LineString) GeoTranslator.transform(geometry, 1, 5);
		}
		if (this.checkValue("GEOMETRY",  this.geometry, geometry)) {
			this.geometry = geometry;
		}
		
	}

	public double getXGuide() {
		return xGuide;
	}

	public void setXGuide(double xGuide) {
		if (this.checkValue("X_GUIDE", this.xGuide, xGuide)) {
			this.xGuide = xGuide;
		}
	}

	public double getYGuide() {
		return yGuide;
	}

	public void setYGuide(double yGuide) {
		if (this.checkValue("Y_GUIDE", this.yGuide, yGuide)) {
			this.yGuide = yGuide;
		}
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		if (this.checkValue("MESH_ID", this.meshId, meshId)) {
			this.meshId = meshId;
		}
	}

}
