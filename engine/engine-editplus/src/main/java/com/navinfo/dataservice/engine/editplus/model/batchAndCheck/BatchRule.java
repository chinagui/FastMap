package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

public class BatchRule { 
	
	private String ruleId;
	private String accessor;
	private String accessorType;
	private Class accessorClass;

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

}
