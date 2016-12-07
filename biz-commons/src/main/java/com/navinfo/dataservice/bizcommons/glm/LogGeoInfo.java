package com.navinfo.dataservice.bizcommons.glm;

/***
 * 关联几何对象名称
 * 
 * @author zhaokk
 * 
 */
public class LogGeoInfo {

	public String getGeoName() {
		return geoName;
	}

	public void setGeoName(String geoName) {
		this.geoName = geoName;
	}

	public int getGeoPid() {
		return geoPid;
	}

	public void setGeoPid(int geoPid) {
		this.geoPid = geoPid;
	}

	public String[] getGrids() {
		return grids;
	}

	public void setGrids(String[] grids) {
		this.grids = grids;
	}

	private String geoName;

	private int geoPid;

	private String[] grids;

}
