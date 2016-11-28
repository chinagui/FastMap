package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.util.Map;
import java.util.Set;

public class CheckRule { 
	
	private String ruleId;
	private String log;
	private String accessor;
	private String accessorType;
	private Class accessorClass;
	private Set<String> objNameSet;
	private Map<String, Set<String>> referSubtableMap;

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

	public Map<String, Set<String>> getReferSubtableMap() {
		return referSubtableMap;
	}

	public void setReferSubtableMap(Map<String, Set<String>> referSubtableMap) {
		this.referSubtableMap = referSubtableMap;
	}

}
