package com.navinfo.dataservice.engine.statics.launcher;

import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
 * @ClassName: StatFeedbackExchanger
 * @author xiaoxiaowen4127
 * @date 2017年5月19日
 * @Description: StatFeedbackExchanger.java
 */
public class StatFeedbackExchanger extends Observable{
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private Map<String,Set<String>> statFeedbacks;

	private StatFeedbackExchanger() {
	}

	private static class SingletonHolder {
		private static final StatFeedbackExchanger INSTANCE = new StatFeedbackExchanger();
	}

	public static StatFeedbackExchanger getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
