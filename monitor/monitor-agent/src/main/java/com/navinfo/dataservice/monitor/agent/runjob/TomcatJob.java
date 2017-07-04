package com.navinfo.dataservice.monitor.agent.runjob;

import java.util.List;

import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.tomcat.TomcatStatInfoLoader;

public class TomcatJob extends AbstractJob{

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	@Override
	public void execute() {
		tomcatRun();
	}


	private static void tomcatRun(){
		long time = getTime();
		List<List<String>> monitorTarget = getMonitorTarget();
		if(monitorTarget.size() > 0){
			TomcatStatInfoLoader.sendTomcatStatInfo(monitorTarget,time);
		}
	}
}
