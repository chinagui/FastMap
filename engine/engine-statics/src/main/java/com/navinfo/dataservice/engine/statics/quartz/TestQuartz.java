package com.navinfo.dataservice.engine.statics.quartz;

import java.util.Date;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

public class TestQuartz {
	Logger logger=LoggerRepos.getLogger(getClass());
	public void doQuartz(){
		logger.info("start"+new Date());
	}
}
