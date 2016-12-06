package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.BatchRuleFactory;

public class BatchCommand extends AbstractCommand {
	private List<String> ruleIdList=new ArrayList<String>();
	private String ruleId;
	//调用的操作名称，根据这个获取需要执行的规则list，与ruleId不能共存，有且仅能有一个存在
	private String operationName;

	public List<String> getRuleIdList() {
		return ruleIdList;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
		ruleIdList.add(ruleId);
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) throws Exception {
		this.operationName = operationName;
		ruleIdList = BatchRuleFactory.getInstance().loadByOperationName(operationName);
	}

}
