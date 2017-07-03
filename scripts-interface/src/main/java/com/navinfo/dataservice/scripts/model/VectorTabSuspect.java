package com.navinfo.dataservice.scripts.model;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @Title: VectorTabSuspect
 * @Package: com.navinfo.dataservice.dao.glm.model.render
 * @Description: 疑似矢量数据
 * @Author: zl
 * @Date: 2017/6/27
 */
public class VectorTabSuspect {

    /**
     * 测线号码
     */
    private int id;
    
    /**
     * 来源
     */
    private String adminCode;
    
    private String length;
    
    private String confidence;
    
    private String data;
    
    private String memo;
    
    /**
     * 坐标
     */
    private String geometry;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdminCode() {
		return adminCode;
	}

	public void setAdminCode(String adminCode) {
		this.adminCode = adminCode;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getConfidence() {
		return confidence;
	}

	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getGeometry() {
		return geometry;
	}

	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}

}
