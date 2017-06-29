package com.navinfo.dataservice.engine.statics.quartz;

import java.util.Date;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;

public class TestQuartz {
	Logger logger=LoggerRepos.getLogger(getClass());
	public void doQuartz(){
		logger.info("start "+DateUtils.dateToString(new Date()));
	}
}
