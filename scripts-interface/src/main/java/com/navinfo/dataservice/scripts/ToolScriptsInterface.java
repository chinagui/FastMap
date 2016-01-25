package com.navinfo.dataservice.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.job.AbstractJobResponse;
import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.UnifiedDb;
import com.navinfo.dataservice.diff.DiffEngine;
import com.navinfo.dataservice.diff.DiffResponse;
import com.navinfo.dataservice.diff.config.DiffConfig;
import com.navinfo.dataservice.expcore.Exporter;
import com.navinfo.dataservice.expcore.Exporter2OracleByFullCopy;
import com.navinfo.dataservice.expcore.Exporter2OracleByScripts;
import com.navinfo.dataservice.expcore.ExporterResult;
import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: ScriptsInterface 
 * @author Xiao Xiaowen 
 * @date 2015-12-29 下午4:35:05 
 * @Description: TODO
 */
public class ToolScriptsInterface {
	public static JSONObject createDb(JSONObject request)throws Exception{
		JSONObject response = new JSONObject();
		String name = (String)request.get("name");
		String type = (String)request.get("type");
		if(StringUtils.isEmpty(type)){
			throw new Exception("ERROR:request/create_db.json中需要设置type属性");
		}
		String descp = (String)request.get("descp");
		String gdbVersion = (String)request.get("gdbVersion");

		DbManager man = new DbManager();
		UnifiedDb db = null;
		db = man.createDb(name,type, descp,gdbVersion);
		response.put("dbId", String.valueOf(db.getDbId()));
		return response;
	}
	public static JSONObject exportData(JSONObject request)throws Exception{
		ExportConfig expConfig = new ExportConfig(request);
		
		Exporter exporter = null;
		if(ExportConfig.MODE_FULL_COPY.equals(expConfig.getExportMode())){
			exporter = new Exporter2OracleByFullCopy(expConfig);
		}else{
			exporter = new Exporter2OracleByScripts(expConfig);
		}
		ExporterResult result = exporter.execute();
		if(result.getStatus()==-1){
			throw new Exception("导出过程出错。");
		}
		JSONObject response = new JSONObject();
		return response;
	}
	public static JSONObject diff(JSONObject request)throws Exception{
		DiffConfig diffConfig = new DiffConfig(request);
		//
		DiffEngine diffEngine = new DiffEngine(diffConfig);
		
		DiffResponse res = diffEngine.execute();
		if(res.getStatus()!=AbstractJobResponse.STATUS_SUCCESS){
			throw new Exception("差分过程出错。");
		}
		JSONObject response = res.generateJson();
		return response;
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
				System.out.println("ERROR:need args:-itype xxx");
				return;
			}
			for(int i=0; i<args.length;i+=2){
			        map.put(args[i], args[i+1]);
		    }
			String itype = map.get("-itype");
			itype = "diff";
			if(StringUtils.isEmpty(itype)){
				System.out.println("ERROR:need args:-itype xxx");
				return;
			}
			JSONObject request=null;
			JSONObject response = null;
			String dir = SystemConfig.getSystemConfig().getValue("scripts.dir");
			if("create_db".equals(itype)){
				request = readJson(dir+"request"+File.separator+"create_db.json");
				response = ToolScriptsInterface.createDb(request);
				writeJson(response,dir+"response"+File.separator+"create_db.json");
			}else if("export_data".equals(itype)){
				request = readJson(dir+"request"+File.separator+"export_data.json");
				response = ToolScriptsInterface.exportData(request);
				writeJson(response,dir+"response"+File.separator+"export_data.json");
			}else if("diff".equals(itype)){
				request = readJson(dir+"request"+File.separator+"diff.json");
				response = ToolScriptsInterface.diff(request);
				writeJson(response,dir+"response"+File.separator+"diff.json");
			}else{
				System.out.println("ERROR:need arg -itype");
			}
			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
}
