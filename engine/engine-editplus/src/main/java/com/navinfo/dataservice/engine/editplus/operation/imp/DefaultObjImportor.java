package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
 * @ClassName: DefaultObjImportor
 * @author xiaoxiaowen4127
 * @date 2016年12月2日
 * @Description: DefaultObjImportor.java
 */
public class DefaultObjImportor {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	public void parseByJsonConfig(JSONObject json)throws Exception{
		if(json==null) {
			log.warn("注意：未传入的解析json对象，request未被初始化");
		}else{
			
			for(Iterator it = json.keys();it.hasNext();){
				String attName = (String)it.next();
				Object attValue = json.get(attName);
				if((attValue==null && (!(attValue instanceof JSONNull)))||StringUtils.isEmpty(attName)){
					log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
					continue;
				}
				setAttrValue(attName,attValue,this.getClass());
			}
		}
	}

	public void setAttrValue(String attName,Object attValue,Class clazz)throws Exception{
		if(StringUtils.isEmpty(attName)||(attValue==null && (!(attValue instanceof JSONNull)))){
			log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
			return;
		}
		try{
			if("objStatus".equals(attName)){
				attName= "opType";
				if("INSERT".equals(attValue)){
					attValue=OperationType.INSERT;
				}else if("UPDATE".equals(attValue)){
					attValue=OperationType.UPDATE;
				}else if("DELETE".equals(attValue)){
					attValue=OperationType.DELETE;
				}
			}
			String methodName = "set"+(char)(attName.charAt(0)-32)+attName.substring(1, attName.length());
			Class[] argtypes = null;//默认String
			
			if(attValue instanceof String){
				argtypes = new Class[]{String.class};
			}else if(attValue instanceof Integer){
				argtypes= new Class[]{int.class};
			}else if(attValue instanceof Double){
				argtypes = new Class[]{double.class};
			}else if(attValue instanceof Boolean){
				argtypes= new Class[]{boolean.class};
			}else if(attValue instanceof OperationType){
				argtypes= new Class[]{OperationType.class};
			}else if(attValue instanceof JSONArray){
				JSONArray attArr = (JSONArray)attValue;
				if(attArr.size()>0){
					for(int i=0;i<attArr.size();i++){
						Object subObj = attArr.get(0);
						if(subObj instanceof String
								||subObj instanceof Integer
								||subObj instanceof Double
								||subObj instanceof Boolean
								){
							argtypes= new Class[]{List.class};
						}else if(subObj instanceof JSONObject){
							argtypes= new Class[]{Map.class};
							Map newAttValue = new HashMap();
							for(Object o:attArr){
								JSONObject jo = (JSONObject)o;
								Object key = jo.get("key");
								Object value = jo.get("value");
								if(key!=null&&value!=null){
									newAttValue.put(key, value);
								}
							}
							attValue=newAttValue;
						}else{
							throw new Exception(attName+"为数组类型，其内部格式为不支持的json结构");
						}
					}
				}else{
					return;
				}
				
			}else if(attValue instanceof JSONObject){
				//sub job
				
			}
			Method method = clazz.getMethod(methodName, argtypes);
			method.invoke(clazz, attValue);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new Exception("Request解析过程中可能未找到方法,原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 获取子表对象
	 * @author Han Shaoming
	 * @param attValue
	 * @return
	 */
	public BasicRow getSubrowName(Object attValue){
		/*IX_POI_NAME
		IX_POI_ADDRESS
		IX_POI_CONTACT
		IX_POI_RESTAURANT
		IX_POI_PARKING
		IX_POI_HOTEL
		IX_POI_CHARGINGSTATION
		IX_POI_CHARGINGPLOT
		IX_POI_GASSTATION
		IX_POI_CHILDREN
		IX_POI_PARENT
		IX_POI_DETAIL*/
		/*if(""){
			
		}else if(){
			
		}else if(){
			
		}else if(){
			
		}else if(){
			
		}else if(){
			
		}else if(){
			
		}else if(){
			
		}*/
		return null;
		
	}
}
