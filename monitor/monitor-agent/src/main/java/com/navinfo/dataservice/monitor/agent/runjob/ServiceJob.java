package com.navinfo.dataservice.monitor.agent.runjob;

import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.service.ServiceStatInfoLoader;

/**
 * 定时任务执行类
 * @ClassName QuartzJob
 * @author Han Shaoming
 * @date 2017年6月16日 上午9:20:40
 * @Description TODO
 */
public class ServiceJob extends AbstractJob {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	@Override
	public void execute() {
		serviceRun();
		
	}
	
	/**
	 * 运行监控任务
	 * @author Han Shaoming
	 */
	private static void serviceRun(){
		long time = getTime();
		System.out.println(time);
		List<List<String>> monitorTarget = getMonitorTarget();
		if(monitorTarget.size() > 0){
			ServiceStatInfoLoader.pushStatInfo(monitorTarget,time);
		}
	}

	

}
