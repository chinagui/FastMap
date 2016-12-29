package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
 * @ClassName: DefaultObjImportor
 * @author xiaoxiaowen4127
 * @date 2016年12月2日
 * @Description: DefaultObjImportor.java
 */

public class DefaultObjImportor extends AbstractOperation{
	protected Map<String,String> errLog = new HashMap<String,String>();
	protected long dbId;
	public long getDbId() {
		return dbId;
	}
	public void setDbId(long dbId) {
		this.dbId = dbId;
	}
	
	public DefaultObjImportor(Connection conn, OperationResult preResult) {
		super(conn, preResult);
	}
	
	public Map<String, String> getErrLog() {
		return errLog;
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		EditJson editJsons = ((DefaultObjImportorCommand)cmd).getEditJson();
		if(editJsons!=null){
			//新增
			List<Map<String, JSONObject>> addJsons = editJsons.getAddJsons();
			for (Map<String, JSONObject> addMap : addJsons) {
				if(addMap!=null&&addMap.size()>0){
					List<BasicObj> objAdd = this.improtAdd(conn, addMap);
					result.putAll(objAdd);
				}
			}
			//修改
			Map<String,Map<Long,JSONObject>> updateJsons = editJsons.getUpdateJsons();
			if(updateJsons!=null&&updateJsons.size()>0){
				List<BasicObj> objUpdate = this.improtUpdate(conn,updateJsons);
				result.putAll(objUpdate);
			}
			//删除
			Map<String,Map<Long,JSONObject>> deleteJsons = editJsons.getDeleteJsons();
			if(deleteJsons!=null&&deleteJsons.size()>0){
				List<BasicObj> objDelete = this.improtDelete(conn, deleteJsons);
				result.putAll(objDelete);
			}
			
		}
	}
	
	/**
	 * 新增数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public List<BasicObj> improtAdd(Connection conn,Map<String, JSONObject> addMap)throws Exception{
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Map.Entry<String, JSONObject> entry : addMap.entrySet()) {
			JSONObject jo = entry.getValue();
			String type = entry.getKey();
			String objType = null;
			//日志
			log.info("新增json数据"+jo.toString());
			try {
				objType = this.getObjType(type);
				BasicObj obj = ObjFactory.getInstance().create(objType);
				this.parseByJsonConfig(jo, obj);
				objList.add(obj);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				errLog.put(objType+"表", StringUtils.isEmpty(e.getMessage())?"新增执行成功":e.getMessage());
			}
		}
		return objList;
	}
	
	/**
	 * 修改数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jso
	 * @param updatePois 
	 * @return
	 * @throws Exception
	 */
	public List<BasicObj> improtUpdate(Connection conn,Map<String,Map<Long,JSONObject>> updateMaps)throws Exception{
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for(Entry<String, Map<Long, JSONObject>> entry : updateMaps.entrySet()){
			String objType = entry.getKey();
			Map<Long, JSONObject> updateMap = entry.getValue();
			if(updateMap != null && updateMap.size()>0){
				try{
					List<BasicObj> list = this.importUpdateByJson(conn, updateMap, objType);
					objList.addAll(list);
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(objType, StringUtils.isEmpty(e.getMessage())?"修改执行成功":e.getMessage());
				}
			}
		}
		return objList;
	}
	
	public List<BasicObj> importUpdateByJson(Connection conn,Map<Long, JSONObject> updateMap,String objType) throws Exception {
		List<BasicObj> objList = new ArrayList<BasicObj>();
		//获取所需的子表
		Set<String> tabNames = null;
		String newObjType = null;
		if("IXPOI".equals(objType)){
			tabNames = DefaultObjSubRowName.getIxPoiTabNames(updateMap);
			newObjType = ObjectName.IX_POI;
		}else if("IXHAMLET".equals(objType)){
			newObjType = ObjectName.IX_HAMLET;
		}else if("ADFACE".equals(objType)){
			newObjType = ObjectName.AD_FACE;
		}else if("ADLINK".equals(objType)){
			newObjType = ObjectName.AD_LINK;
		}else if("ADNODE".equals(objType)){
			newObjType = ObjectName.AD_NODE;
		}
		Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn,newObjType,tabNames,false,updateMap.keySet(),true,true);
		//开始导入
		for (Entry<Long, JSONObject> jo : updateMap.entrySet()) {
			//日志
			log.info("修改json数据"+jo.getValue().toString());
			BasicObj obj = objs.get(jo.getKey());
			if(obj==null){
				errLog.put(Long.toString(jo.getKey()), "日库中没有查到相应的数据");
			}else{
				try{
					if(obj.isDeleted()){
						throw new Exception("该数据已经逻辑删除");
					}else{
						this.parseByJsonConfig(jo.getValue(), obj);
						objList.add(obj);
					}
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(Long.toString(jo.getKey()), StringUtils.isEmpty(e.getMessage())?"修改执行成功":e.getMessage());
				}
			}
		}
		return objList;
	}
	
	/**
	 * 删除数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public List<BasicObj> improtDelete(Connection conn,Map<String,Map<Long,JSONObject>> deleteMaps)throws Exception{
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for(Entry<String, Map<Long, JSONObject>> entry : deleteMaps.entrySet()){
			String objType = entry.getKey();
			Map<Long, JSONObject> deleteMap = entry.getValue();
			if(deleteMap != null && deleteMap.size()>0){
				try{
					List<BasicObj> list = this.importDeleteByJson(conn, deleteMap, objType);
					objList.addAll(list);
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(objType, StringUtils.isEmpty(e.getMessage())?"逻辑删除执行成功":e.getMessage());
				}
			}
		}
		return objList;
	}
	
	public List<BasicObj> importDeleteByJson(Connection conn,Map<Long, JSONObject> deleteMap,String objType)throws Exception {
		List<BasicObj> objList = new ArrayList<BasicObj>();
		//获取所需的子表
		Set<String> tabNames = null;
		String newObjType = null;
		if("IXPOI".equals(objType)){
			tabNames = DefaultObjSubRowName.getIxPoiTabNames(deleteMap);
			newObjType = ObjectName.IX_POI;
		}else if("IXHAMLET".equals(objType)){
			newObjType = ObjectName.IX_HAMLET;
		}else if("ADFACE".equals(objType)){
			newObjType = ObjectName.AD_FACE;
		}else if("ADLINK".equals(objType)){
			newObjType = ObjectName.AD_LINK;
		}else if("ADNODE".equals(objType)){
			newObjType = ObjectName.AD_NODE;
		}
		Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn,newObjType,tabNames,false,deleteMap.keySet(),true,true);
		//开始导入
		for (Map.Entry<Long, JSONObject> jo : deleteMap.entrySet()) {
			//日志
			log.info("删除json数据"+jo.getValue().toString());
			BasicObj obj = objs.get(jo.getKey());
			if(obj==null){
				errLog.put(Long.toString(jo.getKey()), "日库中没有查到相应的数据");
			}else{
				try{
					if(obj.isDeleted()){
						throw new Exception("该数据已经逻辑删除");
					}else{
						//该对象逻辑删除
						obj.deleteObj();
						objList.add(obj);
					}
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(Long.toString(jo.getKey()), StringUtils.isEmpty(e.getMessage())?"删除执行出现空指针错误":e.getMessage());
				}
			}
		}
		return objList;
	}
	
	@SuppressWarnings({"rawtypes" })
	public void parseByJsonConfig(JSONObject json,BasicObj obj)throws Exception{
		if(json==null) {
			log.warn("注意：未传入的解析json对象，request未被初始化");
		}else{
			BasicRow mainrow = obj.getMainrow();
			for(Iterator it = json.keys();it.hasNext();){
				String attName = (String)it.next();
				Object attValue = json.get(attName);
				if((attValue==null && (!(attValue instanceof JSONNull)))||StringUtils.isEmpty(attName)
						||"objStatus".equals(attName)||"rowId".equals(attName)){
					log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。"+attName);
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
						//数据类型转换||"pid".equals(attName)
						if(attValue instanceof Integer){
							Field field = this.getFieldByName(mainrow.getClass(), attName);
							if("Long".equalsIgnoreCase(field.getType().getName())){
								attValue = Long.valueOf((Integer)attValue);
								log.info("转换字段:"+attName);
							}
						}
						String newAttName = this.camelToUnderline(attName);
						mainrow.setAttrByCol(newAttName, attValue);
					}else if(attValue instanceof JSONArray){
							JSONArray attArr = (JSONArray)attValue;
							if(attArr.size()>0){
								for(int i=0;i<attArr.size();i++){
									Object subObj = attArr.get(i);
									if(subObj instanceof JSONObject){
										//为子表
										JSONObject jo = (JSONObject) subObj;
										this.setSubAttrValue(jo, obj, attName);
									}else{
										throw new Exception(attName+"为数组类型，其内部格式为不支持的json结构");
									}
								}
							}
					}else if (attValue instanceof JSONObject) {
						//为子表
						JSONObject subJo = (JSONObject) attValue;
						this.setSubAttrValue(subJo, obj, attName);
					}
				}catch(Exception e){
					log.error(e.getMessage(),e);
					throw new Exception("Request解析过程中可能未找到方法,原因为:"+e.getMessage(),e);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
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
				boolean flag = true;
				if(!subRowList.isEmpty()){
					for (BasicRow basicRow : subRowList) {
						if(basicRow.getRowId().equals(rowId)){
							subRow = basicRow;
							flag = false;
							break;
						}
					}
				}
				if(flag){
					throw new Exception("rowId为:"+rowId+"的子表没有查到");
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
					if((attValue==null && (!(attValue instanceof JSONNull)))
							||StringUtils.isEmpty(attName)||"objStatus".equals(attName)
							||"pid".equals(attName)||"rowId".equals(attName)){
						log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。"+attName);
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
							//数据类型转换
							if(attValue instanceof Integer){
								Field field = this.getFieldByName(subRow.getClass(), attName);
								if("Long".equalsIgnoreCase(field.getType().getName())){
									attValue = Long.valueOf((Integer)attValue);
									log.info("转换字段:"+attName);
								}
							}
							String newAttName = this.camelToUnderline(attName);
							subRow.setAttrByCol(newAttName, attValue);
						}else if(attValue instanceof JSONArray){
							JSONArray attArr = (JSONArray)attValue;
							if(attArr.size()>0){
								for(int i=0;i<attArr.size();i++){
									Object subObj = attArr.get(i);
									if(subObj instanceof JSONObject){
										//为子表
										JSONObject jo = (JSONObject) subObj;
										this.setSubAttrValue(jo, obj, attName);
									}else{
										throw new Exception(attName+"为数组类型，其内部格式为不支持的json结构");
									}
								}
							}
						}else if (attValue instanceof JSONObject) {
							//为子表
							JSONObject subJo = (JSONObject) attValue;
							this.setSubAttrValue(subJo, obj, attName);
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
	 * 获取主表类型
	 * @author Han Shaoming
	 * @return
	 * @throws Exception 
	 */
	public String getObjType(String type) throws Exception{
		//添加所需的子表
		String objType = null;
		if("IXPOI".equals(type)){
			objType = ObjectName.IX_POI;
		}else if("IXHAMLET".equals(type)){
			objType = ObjectName.IX_HAMLET;
		}else if("ADFACE".equals(type)){
			objType = ObjectName.AD_FACE;
		}else if("ADLINK".equals(type)){
			objType = ObjectName.AD_LINK;
		}else if("ADNODE".equals(type)){
			objType = ObjectName.AD_NODE;
		}else{
			throw new Exception("未找到相应的主表类型");
		}
		return objType;
	}
	
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "DefaultObjImportor";
	}
	
	public String camelToUnderline(String param) {
		if (param == null || "".equals(param.trim())) {
			return "";
		}
		int len = param.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = param.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append("_");
				sb.append(c);
			} else {
				sb.append(Character.toUpperCase(c));
			}
		}
		return sb.toString();
	}
	
	public Field getFieldByName(Class<?> clazz,String attName) throws Exception{
		Field field = null;
		Field[] fields = clazz.getDeclaredFields();
		boolean flag = true;
		for (Field fd : fields) {
			if(attName.equals(fd.getName())){
				field = fd;
				flag = false;
				break;
			}
		}
		if(flag){
			Class<?> superclass = clazz.getSuperclass();
			field = getFieldByName(superclass, attName);
		}
		return field;
	}
}
