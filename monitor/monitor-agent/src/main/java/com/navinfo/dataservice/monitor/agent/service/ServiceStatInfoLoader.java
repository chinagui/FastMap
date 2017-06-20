package com.navinfo.dataservice.monitor.agent.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.monitor.agent.model.StatInfo;
import com.navinfo.dataservice.monitor.agent.utils.AgentUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: ServiceStatInfoLoader
 * @author xiaoxiaowen4127
 * @date 2017年6月13日
 * @Description: ServiceStatInfoLoader.java
 */
public class ServiceStatInfoLoader {
	
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private Map<String,Map<String,Object>> statInfoPre = new HashMap<String,Map<String,Object>>();
	
	/**
	 * 推送数据
	 */
	public void pushStatInfo(List<List<String>> monitorTarget){
		for (List<String> list : monitorTarget) {
			String host = list.get(0);
			String port = list.get(1);
			String tomcat = list.get(2);
			//推送数据
			try {
				List<StatInfo> resultList = this.handleStatInfo(host,port,tomcat);
				String msg = AgentUtils.pushData(resultList);
				log.info(msg);
				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("推送数据报错,"+e.getMessage());
			}
		}

	}
	/**
	 * 处理推送的数据
	 * @author Han Shaoming
	 * @param tomcat 
	 * @param port 
	 * @param host 
	 */
	public List<StatInfo> handleStatInfo(String host, String port, String tomcat){
		String url = "http://"+host+":"+port+"/"+tomcat+"/monitoring?part=counterSummaryPerClass&counter=http&format=json&period=jour";
		log.info("service访问地址:"+url);
		List<StatInfo> resultList = new ArrayList<StatInfo>();
		try {
			String data = ServiceInvokeUtil.invokeByGet(url);
			JSONObject jso = JSONObject.fromObject(data);
			JSONArray jsa = jso.getJSONArray("list");
			if(jsa != null){
				for (int i=0;i<jsa.size();i++) {
					JSONObject obj = jsa.getJSONObject(i);
					String name = obj.getString("name");
					String mapKey = host+"/"+tomcat+name;
					int	hits = obj.getInt("hits");
					int durationsSum = obj.getInt("durationsSum");
					int systemErrors = obj.getInt("systemErrors");
					
					Date date = new Date();
					int time = (int) date.getTime();
					
					String metricVisitCount = "fos.service.visitCount";
					String metricResTime = "fos.service.responseTime";
					String metricInterfaceStatus = "fos.service.interfaceStatus";
					String tags = "biz="+tomcat+",name="+name;
					//上一次数据
					int	hitsLast = 0;
					int durationsSumLast = 0;
					int systemErrorsLast = 0;
					if(statInfoPre.size() > 0){
						if(statInfoPre.containsKey(mapKey)){
							Map<String, Object> map = statInfoPre.get(mapKey);
							hitsLast = (int) map.get("hits");
							durationsSumLast = (int) map.get("durationsSum");
							systemErrorsLast = (int) map.get("systemErrors");
							log.info("上一次数据,访问次数:"+hitsLast+"响应时间:"+durationsSumLast+"接口状态:"+systemErrorsLast);
						}
					}
					//处理数据
					float valueVisitCount = hits - hitsLast;
					float valueResTime = 0;
					if(valueVisitCount > 0){
						valueResTime = (durationsSum - durationsSumLast)/valueVisitCount;
					}
					float valueInterfaceStatus = 0;
					//0-正常,1-异常
					if(systemErrors > 0 &&(systemErrors > systemErrorsLast)){
						valueInterfaceStatus = 1;
					}
					log.info("访问次数:"+valueVisitCount+"响应时间:"+valueResTime+"接口状态:"+valueInterfaceStatus);
					//保存数据
					//访问次数
					StatInfo statInfoVisitCount = new StatInfo();
					statInfoVisitCount.setEndpoint(host);
					statInfoVisitCount.setMetric(metricVisitCount);
					statInfoVisitCount.setTimestemp(time);
					statInfoVisitCount.setStep(60);
					statInfoVisitCount.setValue(valueVisitCount);
					statInfoVisitCount.setCounterType("GAUGE");
					statInfoVisitCount.setTags(tags);
					resultList.add(statInfoVisitCount);
					//响应时间
					StatInfo statInfoResTime = new StatInfo();
					statInfoResTime.setEndpoint(host);
					statInfoResTime.setMetric(metricResTime);
					statInfoResTime.setTimestemp(time);
					statInfoResTime.setStep(60);
					statInfoResTime.setValue(valueResTime);
					statInfoResTime.setCounterType("GAUGE");
					statInfoResTime.setTags(tags);
					resultList.add(statInfoResTime);
					//接口状态
					StatInfo statInfoInterfaceStatus = new StatInfo();
					statInfoInterfaceStatus.setEndpoint(host);
					statInfoInterfaceStatus.setMetric(metricInterfaceStatus);
					statInfoInterfaceStatus.setTimestemp(time);
					statInfoInterfaceStatus.setStep(60);
					statInfoInterfaceStatus.setValue(valueInterfaceStatus);
					statInfoInterfaceStatus.setCounterType("GAUGE");
					statInfoInterfaceStatus.setTags(tags);
					resultList.add(statInfoInterfaceStatus);
					
					//保存数据下次使用
					Map<String, Object> mapLast = new HashMap<String, Object>();
					mapLast.put("hits", hits);
					mapLast.put("durationsSum", durationsSum);
					mapLast.put("systemErrors", systemErrors);
					statInfoPre.put(mapKey, mapLast);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("访问地址:"+url+"报错,"+e.getMessage());
		}
		
		return resultList;

	}

}
