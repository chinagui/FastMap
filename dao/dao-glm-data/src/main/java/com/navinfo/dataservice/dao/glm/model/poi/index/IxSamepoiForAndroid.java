package com.navinfo.dataservice.dao.glm.model.poi.index;

/**
 * 因安卓下载需求，扩展poi_num字段
 * @author zhangli
 * 2016.11.29 17:20
 *
 */
public class IxSamepoiForAndroid extends IxSamepoi {
	private String poiNum = "";

	public String getPoiNum() {
		return poiNum;
	}

	public void setPoiNum(String poiNum) {
		this.poiNum = poiNum;
	}
	
}
