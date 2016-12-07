package com.navinfo.dataservice.day2mon;

import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.NiValException;

public class Classifier {
	Logger log = LoggerRepos.getLogger(this.getClass());
	List<NiValException> checkResult;
	public Classifier(List<NiValException> checkResult) {
		super();
		this.checkResult = checkResult;
	}
	public void execute(){
		//TODO:根据检查的结果checkResult，把找到的ruleid，和对应的pid写入到月库的POI_COLUMN_STATUS 表中。初始化对应的status为默认状态；
	}
	
}
