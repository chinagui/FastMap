package com.navinfo.dataservice.dao.glm.model.poi.index;

/**
 * 因安卓下载需求，扩展poi_num字段
 * @author wangdongbin
 *
 */
public class IxPoiParentForAndroid extends IxPoiParent {
	private String poiNum = "";

	public String getPoiNum() {
		return poiNum;
	}

	public void setPoiNum(String poiNum) {
		this.poiNum = poiNum;
	}
	
}
