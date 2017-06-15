package com.navinfo.dataservice.monitor.agent.tomcat;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: TomcatStatInfoLoader
 * @author xiaoxiaowen4127
 * @date 2017年6月13日
 * @Description: TomcatStatInfoLoader.java
 */
public class TomcatStatInfoLoader {

	//读取monitoring 服务中监控的数据
	public static void main(String[] args) {
		String service_url = "http://192.168.4.188:8084/man/monitoring";
		Map<String,String> parMap = new HashMap<String, String>();
		parMap.put("part", "currentRequests"); 
		parMap.put("format", "json"); 
		String json = null; 
		try {
			json = ServiceInvokeUtil.invokeByGet(service_url, parMap);
//			System.out.println(json);
			JSONObject jsonReq = JSONObject.fromObject(json);
			JSONObject jsonobj = jsonReq.getJSONArray("map").getJSONArray(0).getJSONObject(0);
			
			JSONObject memoryInformationsObj = jsonobj.getJSONObject("memoryInformations");
			JSONArray tomcatInformationsListObj = jsonobj.getJSONArray("tomcatInformationsList");
			
//			JSONObject memoryInformationsObj = jsonobj.getJSONObject("memoryInformations");
//			JSONObject memoryInformationsObj = jsonobj.getJSONObject("memoryInformations");
//			
			
			
			
			
			writeJsonFile(jsonReq, "f:/monitor.JSON");
//			System.out.println(jsonReq);
			System.out.println("over.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
