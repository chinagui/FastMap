package com.navinfo.dataservice.day2mon;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.NiValException;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

public class Check {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private OperationResult opResult;

	public Check(OperationResult opResult) {
		super();
		this.opResult = opResult;
	}
	public List<NiValException> execute(){
		List<NiValException> checkResult =new ArrayList<NiValException>();
		//TODO:根据opResult,调用检查框架，执行检查，返回检查结果add到checkResult；
		return checkResult;
	}
}
