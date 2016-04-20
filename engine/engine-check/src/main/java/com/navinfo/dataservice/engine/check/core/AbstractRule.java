package com.navinfo.dataservice.engine.check.core;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

public abstract class AbstractRule {
	private String ruleCode;
	private String ruleDesc;
	private String ruleLog;
	private String status; //E:enabled,X:deleted
	private String ruleClass;
	public String getRuleCode() {
		return ruleCode;
	}
	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}
	public String getRuleDesc() {
		return ruleDesc;
	}
	public void setRuleDesc(String ruleDesc) {
		this.ruleDesc = ruleDesc;
	}
	public String getRuleLog() {
		return ruleLog;
	}
	public void setRuleLog(String ruleLog) {
		this.ruleLog = ruleLog;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRuleClass() {
		return ruleClass;
	}
	public void setRuleClass(String ruleClass) {
		this.ruleClass = ruleClass;
	}
	
	public abstract CheckResult exe(Result editResult);
	public abstract CheckResult exe(Result editResult,Connection conn);
}
