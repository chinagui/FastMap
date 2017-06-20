package com.navinfo.dataservice.monitor.agent.starter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.quartz.QuartzManager;
import com.navinfo.dataservice.monitor.agent.quartz.ServiceJob;
import com.navinfo.dataservice.monitor.agent.quartz.TomcatJob;
import com.navinfo.dataservice.monitor.agent.service.ServiceStatInfoLoader;
import com.navinfo.dataservice.monitor.agent.utils.AgentUtils;

/** 
 * @ClassName: AgentStarter
 * @author xiaoxiaowen4127
 * @date 2017年6月13日
 * @Description: AgentStarter.java
 */
public class AgentStarter {
	
	protected static Logger log = LoggerRepos.getLogger(AgentStarter.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {  
            String service_name = "动态任务调度service"; 
            String tomcat_name = "动态任务调度tomcat";
            QuartzManager.addJob(service_name, ServiceJob.class, "0 0/5 * * * ?");
            QuartzManager.addJob(tomcat_name, TomcatJob.class, "0/10 * * * * ?");
            QuartzManager.startJobs();
            Thread.sleep(50000);
            QuartzManager.removeJob(tomcat_name);
        } catch (Exception e) {  
            e.printStackTrace(); 
            log.error(e.getMessage());
        }

	}
	
	/**
	 * 运行监控任务
	 * @author Han Shaoming
	 */
	public static void serviceRun(){
		List<List<String>> monitorTarget = monitorTarget();
		if(monitorTarget.size() > 0){
			ServiceStatInfoLoader serviceStatInfoLoader = new ServiceStatInfoLoader();
			serviceStatInfoLoader.pushStatInfo(monitorTarget);
		}
	}
	public static void tomcatRun(){
		List<List<String>> monitorTarget = monitorTarget();
		if(monitorTarget.size() > 0){
			System.out.println();
		}
	}
	/**
	 * 获取监控目标
	 * @author Han Shaoming
	 * @return
	 */
	public static List<List<String>> monitorTarget(){
		List<List<String>> monitor = new ArrayList<List<String>>();
		try {
			String [][] datas = {{"192.168.4.188","8081","edit"},{"192.168.4.110","8081","edit"},
					{"192.168.4.188","8082","fcc"},{"192.168.4.110","8082","fcc"},
					{"192.168.4.188","8083","metadata"},{"192.168.4.110","8083","metadata"},
					{"192.168.4.188","8084","man"},{"192.168.4.110","8084","man"},
					{"192.168.4.188","8085","render"},{"192.168.4.110","8085","render"},
					{"192.168.4.188","8086","dropbox"},{"192.168.4.110","8086","dropbox"},
					{"192.168.4.188","8087","job"},{"192.168.4.110","8087","job"},
					{"192.168.4.188","8089","datahub"},{"192.168.4.110","8089","datahub"},
					{"192.168.4.188","8090","statics"},{"192.168.4.110","8090","statics"},
					{"192.168.4.188","8091","mapspotter"},{"192.168.4.110","8091","mapspotter"},
					{"192.168.4.188","8092","column"},{"192.168.4.110","8092","column"},
					{"192.168.4.188","8093","row"},{"192.168.4.110","8093","row"},
					{"192.168.4.188","8094","sys"},{"192.168.4.110","8094","sys"}};
			for(int i=0;i<datas.length;i++){
				String[] data = datas[i];
				String host = data[0];
				String port = data[1];
				String tomcat = data[2];
				try {
					//判断tomcat是否启动
					boolean flag = AgentUtils.tomcatRunSuccess(host, port);
					if(flag){
						List<String> result = new ArrayList<String>();
						result.add(host);
						result.add(port);
						result.add(tomcat);
						monitor.add(result);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("host="+host+",port="+port+",tomcat="+tomcat);
					log.error("host="+host+",port="+port+",tomcat="+tomcat+e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return monitor;
	}
	
}
