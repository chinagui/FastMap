package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

public class IxPoiObjUtil {
	/*
	 * 官方原始中文名称
	 */
	public static IxPoiName getOfficeOriginCHIName(IxPoiObj poiObj){
		List<IxPoiName> subRows=poiObj.getIxPoiNames();
		for(IxPoiName br:subRows){
			if(br.getNameClass()==1&&br.getNameType()==2&&br.getLangCode().equals("CHI")){
				return br;}
			}
		return null;
	}
	
	/*
	 * 官方标准中文名称
	 */
	public static IxPoiName getOfficeStandardCHIName(IxPoiObj poiObj){
		List<IxPoiName> subRows=poiObj.getIxPoiNames();
		for(IxPoiName br:subRows){
			if(br.getNameClass()==1&&br.getNameType()==1&&br.getLangCode().equals("CHI")){
				return br;}
			}
		return null;
	}
}
