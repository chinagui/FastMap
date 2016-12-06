package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
 * @ClassName: DefaultObjImportor
 * @author xiaoxiaowen4127
 * @date 2016年12月2日
 * @Description: DefaultObjImportor.java
 */
@SuppressWarnings({"rawtypes","unused"})
public class DefaultObjImportor {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected long dbId;
	public long getDbId() {
		return dbId;
	}
	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	public void parseByJsonConfig(JSONObject json,BasicObj obj)throws Exception{
		long objPid = 0L;
		BasicRow mainrow = null;
		if(json==null) {
			log.warn("注意：未传入的解析json对象，request未被初始化");
		}else{
			objPid = obj.objPid();
			mainrow = obj.getMainrow();
			for(Iterator it = json.keys();it.hasNext();){
				String attName = (String)it.next();
				Object attValue = json.get(attName);
				if((attValue==null && (!(attValue instanceof JSONNull)))||StringUtils.isEmpty(attName)){
					log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
					continue;
				}
				try{
					if(attValue instanceof String
							||attValue instanceof Integer
							||attValue instanceof Long
							||attValue instanceof Double
							||attValue instanceof Boolean
							||attValue instanceof JSONNull
							){
						mainrow.setAttrByCol(attName, attValue);
					}else if(attValue instanceof JSONArray){
							JSONArray attArr = (JSONArray)attValue;
							if(attArr.size()>0){
								for(int i=0;i<attArr.size();i++){
									Object subObj = attArr.get(0);
									if(subObj instanceof JSONObject){
										//为子表
										BasicRow subRow = obj.createSubRowByName(attName);
										JSONObject jo = (JSONObject) subObj;
										if(subRow != null){
											
										}else{
											throw new Exception("未找到字段名为:"+attName+"的子表");
										}
									}else{
										throw new Exception(attName+"为数组类型，其内部格式为不支持的json结构");
									}
								}
							}
					}else if (attValue instanceof JSONObject) {
						//为子表
						BasicRow subRow = obj.createSubRowByName(attName);
						JSONObject subJo = (JSONObject) attValue;
						if(subRow != null){
							
						}else{
							throw new Exception("未找到字段名为:"+attName+"的子表");
						}
					}
					
				}catch(Exception e){
					log.error(e.getMessage(),e);
					throw new Exception("Request解析过程中可能未找到方法,原因为:"+e.getMessage(),e);
				}
			}
		}
	}

	public void setSubAttrValue(JSONObject json,BasicObj obj,String subRowName)throws Exception{
		BasicRow subRow =null;
		if(json==null) {
			log.warn("注意：未传入的解析json对象，request未被初始化");
		}else{
			String objStatus = json.getString("objStatus");
			//处理子表
			if("INSERT".equals(objStatus)){
				subRow = obj.createSubRowByName(subRowName);
			}else {
				List<BasicRow> subRowList = obj.getSubRowByName(subRowName);
				String rowId = json.getString("rowId");
				for (BasicRow basicRow : subRowList) {
					if(basicRow.getRowId().equals(rowId)){
						subRow = basicRow;
						break;
					}else{
						throw new Exception("rowId为:"+rowId+"的子表没有查到");
					}
				}
				if("UPDATE".equals(objStatus)){
					//不作处理
				}else if("DELETE".equals(objStatus)){
					obj.deleteSubrow(subRow);
					return;
				}
			}
			if(subRow != null){
				for(Iterator it = json.keys();it.hasNext();){
					String attName = (String)it.next();
					Object attValue = json.get(attName);
					if((attValue==null && (!(attValue instanceof JSONNull)))||StringUtils.isEmpty(attName)){
						log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
						continue;
					}
					try{
						if(attValue instanceof String
								||attValue instanceof Integer
								||attValue instanceof Long
								||attValue instanceof Double
								||attValue instanceof Boolean
								||attValue instanceof JSONNull
								){
							subRow.setAttrByCol(attName, attValue);
						}
					}catch(Exception e){
						log.error(e.getMessage(),e);
						throw new Exception("Request解析过程中可能未找到方法,原因为:"+e.getMessage(),e);
					}
				}
			}else{
				throw new Exception("未找到字段名为:"+subRowName+"的子表");
			}
		}
	}
	
	
	/**
	 * 获取查询所需子表
	 * @author Han Shaoming
	 * @return
	 */
	public Set<String> getTabNames(){
		//添加所需的子表
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_CONTACT");
		tabNames.add("IX_POI_ADDRESS");
		tabNames.add("IX_POI_RESTAURANT");
		tabNames.add("IX_POI_CHILDREN");
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_DETAIL");
		return tabNames;
	}
}
