package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

public class DefaultObjSubRowName {
	
	/**
	 * 获取需要查询的子表
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public static Set<String> getIxPoiTabNames(Map<Long, JSONObject> objMap) throws Exception{
		//添加所需的子表
		Set<String> tabNames = new HashSet<String>();
		if(objMap != null && objMap.size()>0){
			for(Entry<Long, JSONObject> entry : objMap.entrySet()){
				JSONObject json = entry.getValue();
				for(Iterator it = json.keys();it.hasNext();){
					String subRowName = (String)it.next();
					if(StringUtils.isEmpty(subRowName)){
						continue;
					}else{
						if("addresses".equals(subRowName)){
							tabNames.add("IX_POI_ADDRESS");
						}else if("audioes".equals(subRowName)){
							tabNames.add("IX_POI_AUDIO");
						}else if("contacts".equals(subRowName)){
							tabNames.add("IX_POI_CONTACT");
						}else if("entryImages".equals(subRowName)){
							tabNames.add("IX_POI_ENTRYIMAGE");
						}else if("flags".equals(subRowName)){
							tabNames.add("IX_POI_FLAG");
						}else if("icons".equals(subRowName)){
							tabNames.add("IX_POI_ICON");
						}else if("names".equals(subRowName)){
							tabNames.add("IX_POI_NAME");
						}else if("parents".equals(subRowName)){
							tabNames.add("IX_POI_PARENT");
						}else if("children".equals(subRowName)){
							tabNames.add("IX_POI_CHILDREN");
						}else if("photos".equals(subRowName)){
							tabNames.add("IX_POI_PHOTO");
						}else if("videoes".equals(subRowName)){
							tabNames.add("IX_POI_VIDEO");
						}else if("parkings".equals(subRowName)){
							tabNames.add("IX_POI_PARKING");
						}else if("tourroutes".equals(subRowName)){
							tabNames.add("IX_POI_TOURROUTE");
						}else if("events".equals(subRowName)){
							tabNames.add("IX_POI_EVENT");
						}else if("details".equals(subRowName)){
							tabNames.add("IX_POI_DETAIL");
						}else if("businesstimes".equals(subRowName)){
							tabNames.add("IX_POI_BUSINESSTIME");
						}else if("chargingstations".equals(subRowName)){
							tabNames.add("IX_POI_CHARGINGSTATION");
						}else if("chargingplots".equals(subRowName)){
							tabNames.add("IX_POI_CHARGINGPLOT");
						}else if("chargingplotPhs".equals(subRowName)){
							tabNames.add("IX_POI_CHARGINGPLOT_PH");
						}else if("buildings".equals(subRowName)){
							tabNames.add("IX_POI_BUILDING");
						}else if("advertisements".equals(subRowName)){
							tabNames.add("IX_POI_ADVERTISEMENT");
						}else if("gasstations".equals(subRowName)){
							tabNames.add("IX_POI_GASSTATION");
						}else if("introductions".equals(subRowName)){
							tabNames.add("IX_POI_INTRODUCTION");
						}else if("attractions".equals(subRowName)){
							tabNames.add("IX_POI_ATTRACTION");
						}else if("hotels".equals(subRowName)){
							tabNames.add("IX_POI_HOTEL");
						}else if("restaurants".equals(subRowName)){
							tabNames.add("IX_POI_RESTAURANT");
						}else if("carrentals".equals(subRowName)){
							tabNames.add("IX_POI_CARRENTAL");
						}else if("samepoiParts".equals(subRowName)){
							tabNames.add("IX_SAMEPOI_PART");
						}else{
							throw new Exception("字段名为:"+subRowName+"的子表未找到");
						}
					}
				}
			}
		}
		return tabNames;
	}
}
