package com.navinfo.dataservice.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;

/** 
 * @ClassName: ScriptsInterface 
 * @author Xiao Xiaowen 
 * @date 2015-12-29 下午4:35:05 
 * @Description: TODO
 */
public class ToolScriptsInterface {
	public static JSONObject exportData(JSONObject request)throws Exception{
		return null;
	}
	public static JSONObject readJson(String fileName)throws Exception{
		File file = new File(fileName);
		try{
			String str = FileUtils.readFileToString(file);
			JSONObject json = JSONObject.fromObject(str);
			return json;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	public static void writeJson(JSONObject json,String fileName)throws Exception{
		File file = new File(fileName);
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(file,true));
	        bw.write(json + "\r\n");
	        bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
            if (bw != null)
                bw.close();
		}
	}
	
	public static void main(String[] args){
		try{
			Map<String,String> map = new HashMap<String,String>();
			if(args.length%2!=0){
				System.out.println("ERROR:need args:-iclass xxx");
				return;
			}
			for(int i=0; i<args.length;i+=2){
			        map.put(args[i], args[i+1]);
		    }
			String iclass = map.get("-iclass");
			if(StringUtils.isEmpty(iclass)){
				System.out.println("ERROR:need args:-iclass xxx");
				return;
			}
			String irequest = map.get("-irequest");
			if(StringUtils.isEmpty(irequest)){
				System.out.println("ERROR:need args:-irequest xxx");
				return;
			}
			JSONObject request=null;
			JSONObject response = null;
			String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
			//初始化context
			JobScriptsInterface.initContext();
			//
			request = readJson(dir+"request"+File.separator+irequest);
			Class<?> tool = Class.forName("com.navinfo.dataservice.scripts."+iclass);
			Method m = tool.getMethod("execute", JSONObject.class);
			response = (JSONObject)(m.invoke(null, request));
			writeJson(response,dir+"response"+File.separator+irequest);
			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
}
