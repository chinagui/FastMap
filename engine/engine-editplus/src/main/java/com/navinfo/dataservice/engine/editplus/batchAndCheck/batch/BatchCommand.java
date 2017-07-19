package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.BatchRuleFactory;

public class BatchCommand extends AbstractCommand {
	private List<String> ruleIdList=new ArrayList<String>();
	private String ruleId;
	//调用的操作名称，根据这个获取需要执行的规则list，与ruleId不能共存，有且仅能有一个存在
	private String operationName;
	//根据业务需要，添加各种参数。例如，有规则需要子任务的业务类型，则 key:"subTaskWorkKind",value:0(0,2,4)
	private Map<String,Object> parameter;

	public List<String> getRuleIdList() {
		return ruleIdList;
	}

	public String getRuleId() {
		return ruleId;
	}

	public Map<String,Object> getParameter() {
		return parameter;
	}

	public void setParameter(Map<String,Object> parameter) {
		this.parameter = parameter;
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
		if(this.ruleIdList==null||ruleIdList.isEmpty()){
			ruleIdList = BatchRuleFactory.getInstance().loadByOperationName(operationName);}
	}

}
