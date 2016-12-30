package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
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
		Set<String> subRowNames = new HashSet<String>();
		if(objMap != null && objMap.size()>0){
			for(Entry<Long, JSONObject> entry : objMap.entrySet()){
				JSONObject json = entry.getValue();
				for(Iterator it = json.keys();it.hasNext();){
					String subRowName = (String)it.next();
					Object attValue = json.get(subRowName);
					if((attValue==null && (!(attValue instanceof JSONNull)))
							||StringUtils.isEmpty(subRowName)||"objStatus".equals(subRowName)){
						continue;
					}
					if(attValue instanceof JSONArray){
						subRowNames.add(subRowName);
						JSONArray attArr = (JSONArray)attValue;
						if(attArr.size()>0){
							for(int i=0;i<attArr.size();i++){
								Object subObj = attArr.get(0);
								if(subObj instanceof JSONObject){
									//为子表
									JSONObject jo = (JSONObject) subObj;
									for(Iterator ite = jo.keys();ite.hasNext();){
										String subRow = (String)ite.next();
										Object attValue01 = jo.get(subRow);
										if(attValue01 instanceof JSONArray||attValue01 instanceof JSONObject){
											subRowNames.add(subRow);
										}
									}
								}else{
									throw new Exception(subRowName+"为数组类型，其内部格式为不支持的json结构");
								}
							}
						}
					}else if (attValue instanceof JSONObject) {
						//为子表
						subRowNames.add(subRowName);
						JSONObject subJo = (JSONObject) attValue;
						for(Iterator it02 = subJo.keys();it02.hasNext();){
							String subRow02 = (String)it02.next();
							Object attValue02 = subJo.get(subRow02);
							if(attValue02 instanceof JSONArray||attValue02 instanceof JSONObject){
								subRowNames.add(subRow02);
							}
						}
					}
				}
				if(!subRowNames.isEmpty()){
					for (String subRowName : subRowNames) {
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
						}else if("nameFlags".equals(subRowName)){
							tabNames.add("IX_POI_NAME_FLAG");
						}else if("samepois".equals(subRowName)){
							tabNames.add("IX_SAMEPOI");
						}else if("nameTones".equals(subRowName)){
							tabNames.add("IX_POI_NAME_TONE");
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
