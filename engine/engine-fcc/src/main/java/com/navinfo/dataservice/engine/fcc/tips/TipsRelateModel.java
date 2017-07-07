package com.navinfo.dataservice.engine.fcc.tips;

import net.sf.json.JSONObject;

/** 
 * @ClassName: TipsRelateModel.java
 * @author y
 * @date 2017-6-26 下午6:40:21
 * @Description: TODO
 *  
 */
public class TipsRelateModel {
	
	JSONObject json; //测线上关联的tips的json 
	
	String lineRowkey ;//新关联的测线tips的rowkey
	
	JSONObject lineGloaction;//新关联的测线的几何
	


	/**
	 * 
	 */
	public TipsRelateModel() {
		super();
	}


	/**
	 * @param json  测线上关联的tips的json 
	 * @param lineRowkey  新关联的测线tips的rowkey
	 * @param lineGloaction 新关联的测线的几何
	 */
	public TipsRelateModel(JSONObject json, String lineRowkey,
			JSONObject lineGloaction) {
		super();
		this.json = json;
		this.lineRowkey = lineRowkey;
		this.lineGloaction = lineGloaction;
	}


	/**
	 * @return the json
	 */
	public JSONObject getJson() {
		return json;
	}


	/**
	 * @param json the json to set
	 */
	public void setJson(JSONObject json) {
		this.json = json;
	}


	/**
	 * @return the lineRowkey
	 */
	public String getLineRowkey() {
		return lineRowkey;
	}


	/**
	 * @param lineRowkey the lineRowkey to set
	 */
	public void setLineRowkey(String lineRowkey) {
		this.lineRowkey = lineRowkey;
	}


	/**
	 * @return the lineGloaction
	 */
	public JSONObject getLineGloaction() {
		return lineGloaction;
	}


	/**
	 * @param lineGloaction the lineGloaction to set
	 */
	public void setLineGloaction(JSONObject lineGloaction) {
		this.lineGloaction = lineGloaction;
	}
	
	
	

}
