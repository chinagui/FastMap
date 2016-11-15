/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc;

import com.vividsolutions.jts.geom.Geometry;

/** 
* @ClassName: TmcPoint 
* @author Zhang Xiaolong
* @date 2016年11月15日 下午4:16:00 
* @Description: TODO
*/
public class TmcPoint {
	private int tmcId;
	
	private int cid;
	
	private String name;
	
	private int lineTmcId;
	
	private int areaTmcId;
	
	private Geometry geo;

	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLineTmcId() {
		return lineTmcId;
	}

	public void setLineTmcId(int lineTmcId) {
		this.lineTmcId = lineTmcId;
	}

	public int getAreaTmcId() {
		return areaTmcId;
	}

	public void setAreaTmcId(int areaTmcId) {
		this.areaTmcId = areaTmcId;
	}

	public Geometry getGeo() {
		return geo;
	}

	public void setGeo(Geometry geo) {
		this.geo = geo;
	}
}
