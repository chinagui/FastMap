package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

public class CheckRule { 
	
	private String ruleId;
	private String log;
	private String accessor;
	private String accessorType;
	private Class accessorClass;

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

}
