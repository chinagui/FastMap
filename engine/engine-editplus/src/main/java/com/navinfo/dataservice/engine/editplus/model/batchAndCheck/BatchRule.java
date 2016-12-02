package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.util.Map;
import java.util.Set;

public class BatchRule { 
	
	private String ruleId;
	private String accessor;
	private String accessorType;
	private Class accessorClass;
	private Set<String> objNameSet;
	private Map<String, Set<String>> referSubtableMap;
	/* 是否修改参考数据。
	 * 例如日编批处理规则FM-BAT-20-194-1，通过poi，获取poi对应的父，修改父的子表。需要将该规则的changeReferData=true*/
	private boolean changeReferData;

	public BatchRule() {
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

	public boolean isChangeReferData() {
		return changeReferData;
	}

	public void setChangeReferData(boolean changeReferData) {
		this.changeReferData = changeReferData;
	}

}
