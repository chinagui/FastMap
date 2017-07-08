package com.navinfo.dataservice.scripts.model;


/**
 * @Title: ShapeModel
 * @Package: com.navinfo.dataservice.dao.glm.model.render
 * @Description: ShapeModel 
 * @Author: zl
 * @Date: 2017/7/8
 */
public class ShapeModel {

    /**
     * 测线号码
     */
    private int id;
    
    private String lable;
    
    private int colour;
    
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

	public String getLable() {
		return lable;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}

	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}

	public String getGeometry() {
		return geometry;
	}

	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}

}
