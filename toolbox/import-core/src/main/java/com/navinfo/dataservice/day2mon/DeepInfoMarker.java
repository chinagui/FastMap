package com.navinfo.dataservice.day2mon;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

public class DeepInfoMarker {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private OperationResult opResult;
	public DeepInfoMarker(OperationResult opResult) {
		super();
		this.opResult = opResult;
	}
	public void execute(){
		//TODO:根据OperationResult进行深度信息打标记；
	}
}
