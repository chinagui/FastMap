package com.navinfo.dataservice.monitor.agent.tomcat;

import java.io.PrintWriter;
import java.util.ArrayList;
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
 * @ClassName: TomcatStatInfoLoader
 * @author xiaoxiaowen4127
 * @date 2017年6月13日
 * @Description: TomcatStatInfoLoader.java
 */
public class TomcatStatInfoLoader {
	protected static Logger log = LoggerRepos.getLogger(TomcatStatInfoLoader.class);
	
	private static Map<String,Map<String,Object>> statInfoPre = new HashMap<String,Map<String,Object>>();
	
	public static void sendTomcatStatInfo(List<List<String>> monitorTarget, long time){
		for (List<String> list : monitorTarget) {
			String ip = list.get(0);
			String port = list.get(1);
			String tomcat = list.get(2);
			//推送数据
			try {
				String url = "http://"+ip+":"+port+"/"+tomcat;
				List<StatInfo> datas = getTomcatInfoList(url,ip,tomcat,port,time);
				String result = AgentUtils.pushData(datas);
				log.info(result);
				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("推送tomcat数据报错,"+e.getMessage());
			}
		}
	}
	
	public static List<StatInfo> getTomcatInfoList(String url,String ip,String tomcat, String port, long time){
		String service_url = url+"/monitoring";
		Map<String,String> parMap = new HashMap<String, String>();
		parMap.put("part", "currentRequests"); 
		parMap.put("format", "json"); 
		
		String service_url_druid = url+"/druid/datasource.json";
		log.info("tomcat访问地址:"+service_url+"?part=currentRequests&format=json");
		log.info("tomcat_jdbc访问地址:"+service_url_druid);
		String json = null; 
		String json_druid = null;
		List<StatInfo> datas =null;
		String endpoint = "FM-service-"+ip;
		try {
			
			datas = new ArrayList<StatInfo>();
			int step = 30;
			
			json = ServiceInvokeUtil.invokeByGet(service_url, parMap);
				JSONObject jsonReq = JSONObject.fromObject(json);
				JSONObject jsonobj = jsonReq.getJSONArray("map").getJSONArray(0).getJSONObject(0);
				
				JSONObject memoryInformationsObj = jsonobj.getJSONObject("memoryInformations");
				double usedMemory = memoryInformationsObj.getDouble("usedMemory");
				double maxMemory = memoryInformationsObj.getDouble("maxMemory");
				double usedPercent = (double)Math.round((usedMemory/maxMemory)*10000)/100;
				//处理gc数据
				double gctm = memoryInformationsObj.getDouble("garbageCollectionTimeMillis");
				String mapKey = ip+"/"+tomcat;
				double gctmLast = 0;
				if(statInfoPre.size() > 0){
					if(statInfoPre.containsKey(mapKey)){
						Map<String, Object> map = statInfoPre.get(mapKey);
						gctmLast = (double) map.get("gctm");
						log.info(mapKey+"上一次数据,垃圾回收响应时间:"+gctmLast);
					}
				}
				log.info(mapKey+"本次数据,垃圾回收响应时间:"+gctm);
				double gctmPercent = (double)Math.round(((gctm-gctmLast)/(step*1000))*10000)/100;
						
				StatInfo statInfo_memo = new StatInfo();
				statInfo_memo.setEndpoint(endpoint);
				statInfo_memo.setMetric("fos.tomcat.memoUsed");
				statInfo_memo.setTags("biz="+tomcat+",port="+port);
				statInfo_memo.setValue(usedPercent);
				statInfo_memo.setTimestemp(time);
				statInfo_memo.setStep(step);
				
				StatInfo statInfo_gc = new StatInfo();
				statInfo_gc.setEndpoint(endpoint);
				statInfo_gc.setMetric("fos.tomcat.gc");
				statInfo_gc.setTags("biz="+tomcat+",port="+port);
				statInfo_gc.setValue(gctmPercent);
				statInfo_gc.setTimestemp(time);
				statInfo_gc.setStep(step);
				
				datas.add(statInfo_memo);
				datas.add(statInfo_gc);
				
				//保存数据下次使用
				Map<String, Object> mapLast = new HashMap<String, Object>();
				mapLast.put("gctm", gctm);
				statInfoPre.put(mapKey, mapLast);
				
			json_druid = ServiceInvokeUtil.invokeByGet(service_url_druid);
				JSONObject jsonReq_druid = JSONObject.fromObject(json_druid);
				JSONArray contentArr = jsonReq_druid.getJSONArray("Content");
				for(Object dbObj : contentArr){
					JSONObject dbJobj=(JSONObject)dbObj;
					String dbUserName = dbJobj.getString("UserName");//数据库名称
//					int totalConn = dbJobj.getInt("PoolingCount");//当前池连接数
					int activeCount = dbJobj.getInt("ActiveCount");//当前活跃连接数
					int LogicConnectCount = dbJobj.getInt("LogicConnectCount");//逻辑连接建立总数
					int LogicCloseCount = dbJobj.getInt("LogicCloseCount");//逻辑连接关闭总数
//					int PhysicalConnectCount = dbJobj.getInt("PhysicalConnectCount");//物理连接建立总数
//					int PhysicalCloseCount = dbJobj.getInt("ActiveCount");//物理关闭总数
					int unclosedCount = (LogicConnectCount-LogicCloseCount);
					StatInfo statInfo_db_unclosed = new StatInfo();
					statInfo_db_unclosed.setEndpoint(endpoint);
					statInfo_db_unclosed.setMetric("fos.tomcat.jdbc.unclosedConn");
					statInfo_db_unclosed.setTags("biz="+tomcat+",port="+port+",db="+dbUserName);
					statInfo_db_unclosed.setValue(unclosedCount);
					statInfo_db_unclosed.setTimestemp(time);
					statInfo_db_unclosed.setStep(step);
					
					StatInfo statInfo_db_active = new StatInfo();
					statInfo_db_active.setEndpoint(endpoint);
					statInfo_db_active.setMetric("fos.tomcat.jdbc.activeConn");
					statInfo_db_active.setTags("biz="+tomcat+",port="+port+",db="+dbUserName);
					statInfo_db_active.setValue(activeCount);
					statInfo_db_active.setTimestemp(time);
					statInfo_db_active.setStep(step);
					
					datas.add(statInfo_db_unclosed);
					datas.add(statInfo_db_active);
					
				}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datas;
	}

	//读取monitoring 服务中监控的数据
	public static void main(String[] args) {

	}
	
	public static void writeJsonFile(JSONObject ja,String fileName) throws Exception {

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
//			for (int j = 0; j < ja.size(); j++) {
				pw.println(ja.toString());
//			}
		} catch (Exception e) {
			throw e;
		} finally {
			if(pw!=null){
				pw.close();
			}

		}
	}

	
}
