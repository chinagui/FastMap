package com.navinfo.dataservice.monitor.agent.runjob;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.monitor.agent.starter.ConfigFileHandle;
import com.navinfo.dataservice.monitor.agent.utils.AgentUtils;

/**
 * 任务调度
 * @ClassName AbstractJob
 * @author Han Shaoming
 * @date 2017年6月26日 下午2:07:46
 * @Description TODO
 */
public abstract class AbstractJob {
	
	protected static Logger log = LoggerRepos.getLogger(AbstractJob.class);
	
	/**
	 * 所有都要实现该执行方法，任务被调度时会调用
	 */
	public abstract void execute();
	

	//时间戳
	public static long getTime() {
		Date date = new Date();
		long time =  date.getTime();
		return time;
		
	}
	/**
	 * 获取监控目标
	 * @author Han Shaoming
	 * @return
	 */
	public static List<List<String>> getMonitorTarget(){
		List<List<String>> monitor = new ArrayList<List<String>>();
		try {
//			String [][] datas = {{"192.168.4.188","8081","edit"},{"192.168.4.110","8081","edit"},
//					{"192.168.4.188","8082","fcc"},{"192.168.4.110","8082","fcc"},
//					{"192.168.4.188","8083","metadata"},{"192.168.4.110","8083","metadata"},
//					{"192.168.4.188","8084","man"},{"192.168.4.110","8084","man"},
//					{"192.168.4.188","8085","render"},{"192.168.4.110","8085","render"},
//					{"192.168.4.188","8086","dropbox"},{"192.168.4.110","8086","dropbox"},
//					{"192.168.4.188","8087","job"},{"192.168.4.110","8087","job"},
//					{"192.168.4.188","8089","datahub"},{"192.168.4.110","8089","datahub"},
//					{"192.168.4.188","8090","statics"},{"192.168.4.110","8090","statics"},
//					{"192.168.4.188","8091","mapspotter"},{"192.168.4.110","8091","mapspotter"},
//					{"192.168.4.188","8092","column"},{"192.168.4.110","8092","column"},
//					{"192.168.4.188","8093","row"},{"192.168.4.110","8093","row"},
//					{"192.168.4.188","8094","sys"},{"192.168.4.110","8094","sys"},
//					{"192.168.4.188","8095","collector"},{"192.168.4.110","8095","collector"},
//					{"192.168.4.188","8096","dealership"},{"192.168.4.110","8096","dealership"}};
			List<Map<String, String>> monitorServer = ConfigFileHandle.getMonitorServer();
			log.info("待监控的服务:"+monitorServer.toString());
			for (Map<String, String> map : monitorServer) {
				String host = map.get("host");
				String port = map.get("port");
				String tomcat = map.get("name");
				try {
					//判断tomcat是否启动
					boolean flag = AgentUtils.tomcatRunSuccess(host, port);
					if(flag){
						List<String> result = new ArrayList<String>();
						result.add(host);
						result.add(port);
						result.add(tomcat);
						monitor.add(result);
					}else{
						log.info("服务未启动,host="+host+",port="+port+",tomcat="+tomcat);
					}
				} catch (Exception ex) {
					System.out.println("host="+host+",port="+port+",tomcat="+tomcat);
					log.error("服务未启动,host="+host+",port="+port+",tomcat="+tomcat+",错误信息:"+ex.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}
		return monitor;
	}
	
}
