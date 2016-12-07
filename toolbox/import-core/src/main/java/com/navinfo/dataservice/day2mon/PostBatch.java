package com.navinfo.dataservice.day2mon;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

public class PostBatch {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private OperationResult opResult ;

	public PostBatch(OperationResult opResult) {
		super();
		this.opResult = opResult;
	}
	public OperationResult execute(){
		OperationResult opResult = this.opResult;
		//TODO:根据opResult,调用批处理框架，执行后批处理；修改opResult
		return opResult;
	}
}
