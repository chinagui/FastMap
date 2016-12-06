package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.util.List;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

public class CheckCommand extends AbstractCommand {
	private List<String> ruleIdList;
	//isErrorReturn为ture，表示有错误log，则直接停止后续检查；false则继续执行，最后检查结果统一返回
	//isSaveResult=true，则检查结果保存；否则不保存检查结果
	private boolean isSaveResult=true;
	private boolean isErrorReturn=false;//errorIgnore

	public List<String> getRuleIdList() {
		return ruleIdList;
	}

	public void setRuleIdList(List<String> ruleIdList) {
		this.ruleIdList = ruleIdList;
	}

	public boolean isSaveResult() {
		return isSaveResult;
	}

	public void setSaveResult(boolean isSaveResult) {
		this.isSaveResult = isSaveResult;
	}

	public boolean isErrorReturn() {
		return isErrorReturn;
	}

	public void setErrorReturn(boolean isErrorReturn) {
		this.isErrorReturn = isErrorReturn;
	}
}
