package com.navinfo.dataservice.engine.statics.launcher;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
 * @ClassName: GroupStatJobLauncher
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: GroupStatJobLauncher.java
 */
public class GroupStatJobLauncher {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private GroupStatJobLauncher() {
	}

	private static class SingletonHolder {
		private static final GroupStatJobLauncher INSTANCE = new GroupStatJobLauncher();
	}

	public static GroupStatJobLauncher getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	
}
