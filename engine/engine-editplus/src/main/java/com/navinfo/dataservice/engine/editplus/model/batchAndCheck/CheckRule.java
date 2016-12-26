package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CheckRule { 
	
	private String ruleId;
	private String log;
	private String accessor;
	private String accessorType;
	private Class accessorClass;
	private Set<String> objNameSet;
	private Map<String, Set<String>> referSubtableMap;
	private int ruleLevel;//1 不可忽略 2可忽略

	public CheckRule() {
		// TODO Auto-generated constructor stub
	}
	
	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getAccessor() {
		return accessor;
	}

	public void setAccessor(String accessor) throws Exception {
		this.accessor = accessor;
		this.setAccessorClass(Class.forName(accessor));
	}

	public String getAccessorType() {
		return accessorType;
	}

	public void setAccessorType(String accessorType) {
		this.accessorType = accessorType;
	}

	public Class getAccessorClass() {
		return accessorClass;
	}

	public void setAccessorClass(Class accessorClass) {
		this.accessorClass = accessorClass;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public Set<String> getObjNameSet() {
		return objNameSet;
	}

	public void setObjNameSet(Set<String> objNameSet) {
		this.objNameSet = objNameSet;
	}
	
	public void setObjNameSet(String objNameString) {
		this.objNameSet = new HashSet<String>();
		if(objNameString!=null && !objNameString.isEmpty()){
			String[] objs = objNameString.split(",");
			for(int i=0;i<objs.length;i++){
				this.objNameSet.add(objs[i]);
			}}
	}

	public Map<String, Set<String>> getReferSubtableMap() {
		return referSubtableMap;
	}
	
	public void setReferSubtableMap(String referSubtableStr) {
		if(referSubtableStr==null){return;}
		JSONObject referJson = JSONObject.fromObject(referSubtableStr);
		this.referSubtableMap =new HashMap<String, Set<String>>();
		for(Object key:referJson.keySet()){
			JSONArray subtable = referJson.getJSONArray((String) key);
			Set<String> subtableList=new HashSet<String>();
			for(Object tableTmp:subtable){
				subtableList.add((String) tableTmp);
			}
			referSubtableMap.put((String) key,subtableList);
		}
	}

	public void setReferSubtableMap(Map<String, Set<String>> referSubtableMap) {
		this.referSubtableMap = referSubtableMap;
	}

	public int getRuleLevel() {
		return ruleLevel;
	}

	public void setRuleLevel(int ruleLevel) {
		this.ruleLevel = ruleLevel;
	}

}
