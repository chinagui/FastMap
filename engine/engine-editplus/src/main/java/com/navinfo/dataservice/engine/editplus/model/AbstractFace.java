package com.navinfo.dataservice.engine.editplus.model;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.glm.NonObjPidException;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: AbstractFace
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AbstractFace.java
 */
public abstract class AbstractFace extends BasicRow{

	public AbstractFace(long objPid) {
		super(objPid);
	}

	protected long facePid;
	protected Geometry geometry;
	protected double area;
	protected double perimeter;
	protected int meshId;
	protected List<AbstractFaceTopo> topos = new ArrayList<AbstractFaceTopo>();
	
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public double getArea() {
		return area;
	}
	public void setArea(double area) {
		this.area = area;
	}
	public double getPerimeter() {
		return perimeter;
	}
	public void setPerimeter(double perimeter) {
		this.perimeter = perimeter;
	}
	public int getMeshId() {
		return meshId;
	}
	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}
	public List<AbstractFaceTopo> getTopos() {
		return topos;
	}
	public void setTopos(List<AbstractFaceTopo> topos) {
		this.topos = topos;
	}
	
	public AbstractFace copyFace(long pid,Geometry geo){
		return null;
	}
}
