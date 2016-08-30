package com.navinfo.dataservice.engine.edit.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbServer;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.navicommons.database.sql.RunnableSQL;

/** 
 * @ClassName: BasicRow
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: BasicRow.java
 */
public abstract class BasicRow {
	protected Logger log = Logger.getLogger(this.getClass());
	protected OperationType opType=OperationType.INITIALIZE;
	protected String rowId;
	protected Map<String,Object> oldValues=null;
	public abstract String tableName();
	public abstract ObjType objType();
	
	public OperationType getOpType() {
		return opType;
	}
	public void setOpType(OperationType opType) {
		this.opType = opType;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	public Map<String, Object> getOldValues() {
		return oldValues;
	}
	public void setOldValues(Map<String, Object> oldValues) {
		this.oldValues = oldValues;
	}
	/**
	 * 行级记录复制,新Row的OperationType为insert
	 * 新生成rowId
	 * @return
	 */
	public BasicRow copyRow(){
		
		return null;
	}
	public RunnableSQL generateSql(){
		return null;
	}
	/**
	 * colNames为空会获取全部属性值
	 * @param colNames
	 * @return
	 */
	public Map<String,Object> getAttrs(Collection<String> colNames){
		Map<String,Object> attrs = new HashMap<String,Object>();
		return attrs;
	}
	public Object getAttrByColName(String colName){
		return null;
	}
	public boolean checkValue(String colName,int oldValue,int newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,double oldValue,double newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,float oldValue,float newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,boolean oldValue,boolean newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,long oldValue,long newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,Object oldValue,Object newValue){
		if(oldValue==null&&newValue==null)return false;
		if(oldValue!=null&&oldValue.equals(newValue))return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public <T> void setAttrByCol(String colName,T newValue)throws Exception{
		//colName->getter
		String getter=colName2Getter(colName);
		Method methodGetter = this.getClass().getMethod(getter);
		Object oldValue = methodGetter.invoke(this);
		if(checkValue(colName,oldValue,newValue)){
			String setter=colName2Setter(colName);
			Class[] argtypes = null;
			if(newValue instanceof Integer){
				argtypes= new Class[]{int.class};
			}else if(newValue instanceof Double){
				argtypes = new Class[]{double.class};
			}else if(newValue instanceof Boolean){
				argtypes= new Class[]{boolean.class};
			}else if(newValue instanceof Float){
				argtypes= new Class[]{float.class};
			}else if(newValue instanceof Long){
				argtypes= new Class[]{long.class};
			}else{
				argtypes = new Class[]{newValue.getClass()};
			}
			Method method = this.getClass().getMethod(setter,argtypes);
			method.invoke(this, newValue);
		}
	}
	/**
	 * 有特殊字段的表重写此方法
	 * @param colName
	 * @return
	 */
	public String colName2Getter(String colName){
		StringBuilder sb = new StringBuilder();
		sb.append("get");
		for(String s:colName.split("_")){
			
			char c = s.charAt(0);
			c=(char)(c-32);
			sb.append(c);
			sb.append(s.substring(1, s.length()));
		}
		return sb.toString();
	}
	/**
	 * 有特殊字段的表重写此方法
	 * @param colName
	 * @return
	 */
	public String colName2Setter(String colName){
		StringBuilder sb = new StringBuilder();
		sb.append("set");
		for(String s:colName.split("_")){
			char c = s.charAt(0);
			c=(char)(c-32);
			sb.append(c);
			sb.append(s.substring(1, s.length()));
		}
		return sb.toString();
	}
	public String identity(){
		return rowId;
	}
	public int hashCode(){
		return rowId==null?"".hashCode():rowId.hashCode();
	}
	/**
	 * 如果rowId==null,不比较
	 */
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof BasicRow
				&&rowId!=null&&rowId.equals(((BasicRow) anObject).getRowId())){
			return true;
		}else{
			return false;
		}
	}
}
