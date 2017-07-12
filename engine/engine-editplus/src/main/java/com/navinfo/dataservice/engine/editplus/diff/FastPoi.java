package com.navinfo.dataservice.engine.editplus.diff;

import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * 
 * @author jicaihua
 *业务下挂mr 瘦身poi
 */
public class FastPoi {
	private String uuid="";
	private String poiNum;// -- 四维poi_num
	
	private String name="";
	private String addr="";
	private int pid=0;
	private Double xGuide=0.0; 
	private Double yGuide=0.0;
	private Geometry geometry;
	private String shortName="";
	/**
	 * Longitude
	 */
	private Double x=0.0; 
	public Double getxGuide() {
		return xGuide;
	}
	public void setxGuide(Double xGuide) {
		this.xGuide = xGuide;
	}
	public Double getyGuide() {
		return yGuide;
	}
	public void setyGuide(Double yGuide) {
		this.yGuide = yGuide;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	/**
	 * Latitude
	 */
	private Double y=0.0;
	private String tel="";
	private String postCode="";
	private String kindCode = "";
	private String rowkey;
	private String provnm="";
	private String chain = "";
	
	public String getProvnm() {
		return provnm;
	}
	public void setProvnm(String provnm) {
		this.provnm = provnm;
	}
	public String getRowkey() {
		return rowkey;
	}
	public void setRowkey(String rowkey) {
		this.rowkey = rowkey;
	}
	
	public String getKindCode() {
		return kindCode;
	}
	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public Double getX() {
		return x;
	}
	public void setX(Double x) {
		this.x = x;
	}
	public Double getY() {
		return y;
	}
	public void setY(Double y) {
		this.y = y;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getChain() {
		return chain;
	}
	public void setChain(String chain) {
		this.chain = chain;
	}
	public JSONObject toJson() {
		JSONObject jsonObject = JSONObject.fromObject(this);
		return jsonObject;
	}
	public String getPoiNum() {
		return poiNum;
	}
	public void setPoiNum(String poiNum) {
		this.poiNum = poiNum;
	}
	/**
	 * @return the pid
	 */
	public int getPid() {
		return pid;
	}
	/**
	 * @param pid the pid to set
	 */
	public void setPid(int pid) {
		this.pid = pid;
	}
	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}
	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

}
