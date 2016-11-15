/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc;

import com.vividsolutions.jts.geom.Geometry;

/** 
* @ClassName: TmcLine 
* @author Zhang Xiaolong
* @date 2016年11月15日 下午3:26:44 
* @Description: TODO
*/
public class TmcLine {
	private int tmcId;
	
	private String name;
	
	private int cid;
	
	private int areaTmcId;
	
	private int upLineTmcId;
	
	private Geometry geometry;

	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}
	
	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getAreaTmcId() {
		return areaTmcId;
	}

	public void setAreaTmcId(int areaTmcId) {
		this.areaTmcId = areaTmcId;
	}

	public int getUpLineTmcId() {
		return upLineTmcId;
	}

	public void setUpLineTmcId(int upLineTmcId) {
		this.upLineTmcId = upLineTmcId;
	}
}
