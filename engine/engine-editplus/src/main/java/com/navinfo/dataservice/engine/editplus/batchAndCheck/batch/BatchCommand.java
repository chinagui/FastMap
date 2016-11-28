package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import java.util.List;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

public class BatchCommand extends AbstractCommand {
	private List<String> ruleIdList;

	public List<String> getRuleIdList() {
		return ruleIdList;
	}

	public void setRuleIdList(List<String> ruleIdList) {
		this.ruleIdList = ruleIdList;
	}
}
