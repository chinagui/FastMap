package com.navinfo.dataservice.api.man.model;

import java.util.HashMap;
import java.util.Map;

public class BaseObj{
	
	private Map<String,Object> oldValues=null;//存储变化字段的旧值，key:col_name,value：旧值
	public Map<String, Object> getOldValues() {
		return oldValues;
	}
	public void setOldValues(Map<String, Object> oldValues) {
		this.oldValues = oldValues;
	}
	public boolean checkValue(String colName,int oldValue,int newValue){
		if(newValue==oldValue)return false;
		if(oldValues==null){
			oldValues = new HashMap<String,Object>();
			oldValues.put(colName, oldValue);
		}else{
			//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if(!oldValues.containsKey(colName)){
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}
	public boolean checkValue(String colName,double oldValue,double newValue){
		if(newValue==oldValue)return false;
		if(oldValues==null){
			oldValues = new HashMap<String,Object>();
			oldValues.put(colName, oldValue);
		}else{
			//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if(!oldValues.containsKey(colName)){
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}
	public boolean checkValue(String colName,float oldValue,float newValue){
		if(newValue==oldValue)return false;
		if(oldValues==null){
			oldValues = new HashMap<String,Object>();
			oldValues.put(colName, oldValue);
		}else{
			//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if(!oldValues.containsKey(colName)){
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}
	public boolean checkValue(String colName,boolean oldValue,boolean newValue){
		if(newValue==oldValue)return false;
		if(oldValues==null){
			oldValues = new HashMap<String,Object>();
			oldValues.put(colName, oldValue);
		}else{
			//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if(!oldValues.containsKey(colName)){
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}
	public boolean checkValue(String colName,long oldValue,long newValue){
		if(newValue==oldValue)return false;
		if(oldValues==null){
			oldValues = new HashMap<String,Object>();
			oldValues.put(colName, oldValue);
		}else{
			//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if(!oldValues.containsKey(colName)){
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}
	public boolean checkValue(String colName,Object oldValue,Object newValue){
		if(oldValue==null&&newValue==null)return false;
		if(oldValue!=null&&oldValue.equals(newValue))return false;//所有Object类型都通用
		//处理String的null和""的问题
		if((oldValue==null&&newValue.equals(""))
				||(newValue==null&&oldValue.equals(""))){
			return false;
		}
		
		if(oldValues==null){
			oldValues = new HashMap<String,Object>();
			oldValues.put(colName, oldValue);
		}else{
			//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if(!oldValues.containsKey(colName)){
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}

}
