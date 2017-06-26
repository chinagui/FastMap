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
	
	public static void sendTomcatStatInfo(List<List<String>> monitorTarget){
		for (List<String> list : monitorTarget) {
			String ip = list.get(0);
			String port = list.get(1);
			String tomcat = list.get(2);
			//推送数据
			try {
				String url = "http://"+ip+":"+port+"/"+tomcat;
				List<StatInfo> datas = getTomcatInfoList(url,ip);
				String result = AgentUtils.pushData(datas);
				log.info(result);
				
			} catch (Exception e) {
				e.printStackTrace();
				log.error("推送tomcat数据报错,"+e.getMessage());
			}
		}
	}
	
	public static List<StatInfo> getTomcatInfoList(String url,String ip){
		String service_url = url+"/monitoring";
		Map<String,String> parMap = new HashMap<String, String>();
		parMap.put("part", "currentRequests"); 
		parMap.put("format", "json"); 
		
		String service_url_druid = url+"/druid/datasource.json";
		
		String json = null; 
		String json_druid = null;
		List<StatInfo> datas =null;
		try {
			
			datas = new ArrayList<StatInfo>();
			
			json = ServiceInvokeUtil.invokeByGet(service_url, parMap);
				JSONObject jsonReq = JSONObject.fromObject(json);
				JSONObject jsonobj = jsonReq.getJSONArray("map").getJSONArray(0).getJSONObject(0);
				
				JSONObject memoryInformationsObj = jsonobj.getJSONObject("memoryInformations");
				double usedMemory = memoryInformationsObj.getDouble("usedMemory");
				double gctm = memoryInformationsObj.getDouble("garbageCollectionTimeMillis");
				StatInfo statInfo_memo = new StatInfo();
				statInfo_memo.setEndpoint(ip);
				statInfo_memo.setMetric("fos.tomcat.memoUsed");
				statInfo_memo.setTags("biz=man");
				statInfo_memo.setValue(usedMemory);
				StatInfo statInfo_gc = new StatInfo();
				statInfo_gc.setEndpoint(ip);
				statInfo_gc.setMetric("fos.tomcat.gc");
				statInfo_gc.setTags("biz=man");
				statInfo_gc.setValue(gctm);
				
				datas.add(statInfo_memo);
				datas.add(statInfo_gc);
				
			json_druid = ServiceInvokeUtil.invokeByGet(service_url_druid);
				JSONObject jsonReq_druid = JSONObject.fromObject(json_druid);
				JSONArray contentArr = jsonReq_druid.getJSONArray("Content");
				for(Object dbObj : contentArr){
					JSONObject dbJobj=(JSONObject)dbObj;
					String dbUserName = dbJobj.getString("UserName");//数据库名称
					int totalConn = dbJobj.getInt("PoolingCount");//当前池连接数
					int activeCount = dbJobj.getInt("ActiveCount");//当前活跃连接数
					StatInfo statInfo_db_unclosed = new StatInfo();
					statInfo_db_unclosed.setEndpoint(ip);
					statInfo_db_unclosed.setMetric("fos.tomcat.jdbc.unclosedConn");
					statInfo_db_unclosed.setTags("biz=man,db="+dbUserName);
					statInfo_db_unclosed.setValue(totalConn - activeCount);
					
					StatInfo statInfo_db_active = new StatInfo();
					statInfo_db_active.setEndpoint(ip);
					statInfo_db_active.setMetric("fos.tomcat.jdbc.activeConn");
					statInfo_db_active.setTags("biz=man,db="+dbUserName);
					statInfo_db_active.setValue(activeCount);
					
					datas.add(statInfo_db_unclosed);
					datas.add(statInfo_db_active);
					
				}
				
			System.out.println();
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
