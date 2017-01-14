package com.navinfo.dataservice.engine.fcc.photo;

/** 
 * @ClassName: ExpPhotoInfo.java
 * @author y
 * @date 2017-1-14 上午10:22:07
 * @Description: 照片导出model辅助类
 *  
 */
public class ExpPhotoInfo {
	
	String id ;  //照片的id(rowkey)
	String tipsType;//照片所属的tips的类型，详见tips分类
	int tipsStage;//照片所属的tips的状态，详见规格说明
	
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the tipsType
	 */
	public String getTipsType() {
		return tipsType;
	}
	/**
	 * @param tipsType the tipsType to set
	 */
	public void setTipsType(String tipsType) {
		this.tipsType = tipsType;
	}
	/**
	 * @return the tipsStage
	 */
	public int getTipsStage() {
		return tipsStage;
	}
	/**
	 * @param tipsStage the tipsStage to set
	 */
	public void setTipsStage(int tipsStage) {
		this.tipsStage = tipsStage;
	}
	
	
	
	
	

}
