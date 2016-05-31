package com.navinfo.dataservice.scripts;

import java.io.File;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
* @ClassName: refreshFmgdbRoad 
* @author Xiao Xiaowen 
* @date 2016年5月26日 下午5:42:06 
* @Description: TODO
*  
*/
public class RefreshFmgdbRoad {
	
	public static JSONObject refresh(JSONObject request){
		for(Iterator it = request.keys();it.hasNext();){
			String attName = (String)it.next();
			System.out.print(attName+":");
			Object value = request.get(attName);
			System.out.print(value.getClass().getSimpleName()+",");
			if(value instanceof String){
				System.out.println("String");
			}else if(value instanceof Integer){
				System.out.println("Integer");
			}else if(value instanceof Boolean){
				System.out.println("Boolean");
			}else if(value instanceof JSONArray){
				System.out.println("JSONArray");
			}else if(value instanceof JSONNull){
				System.out.println("JSONNull");
			}
		}
		return null;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JSONObject request = null;
			JSONObject response = null;
//			String dir = SystemConfigFactory.getSystemConfig().getValue(
//					"scripts.dir");
			String dir = "F:\\Fm_Projects_Doc\\scripts\\";
			request = ToolScriptsInterface.readJson(dir + "request"
					+ File.separator + "test.json");
			response = refresh(request);
//			ToolScriptsInterface.writeJson(response, dir + "response"
//					+ File.separator + "sampleJobB.json");
//
//			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}

	}
}
