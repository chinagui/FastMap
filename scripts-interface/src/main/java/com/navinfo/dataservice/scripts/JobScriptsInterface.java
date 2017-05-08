package com.navinfo.dataservice.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/** 
* @ClassName: ScriptsInterface 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午3:35:24 
* @Description: TODO
*  
*/
public class JobScriptsInterface {
	public static JSONObject execute(String type,JSONObject request)throws Exception{
		JobInfo jobInfo = new JobInfo(0,UuidUtils.genUuid());
		jobInfo.setType(type);
		jobInfo.setRequest(request);
		jobInfo.setTaskId(0);
		//设置jobId
//		jobInfo.setId(963);
		AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
		job.run();
		return job.getJobInfo().getResponse();
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
			if(StringUtils.isEmpty(itype)){
				System.out.println("ERROR:need args:-itype xxx");
				return;
			}
			String irequest = map.get("-irequest");
			if(StringUtils.isEmpty(irequest)){
				System.out.println("ERROR:need args:-irequest xxx");
				return;
			}
			String iresponse = map.get("-iresponse");
			if(StringUtils.isEmpty(iresponse)){
				System.out.println("ERROR:need args:-iresponse xxx");
				return;
			}
			JSONObject request=null;
			JSONObject response = null;
//			String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
			String dir = "F:\\Fm_Projects_Doc\\scripts\\";
			request = readJson(dir+"request"+File.separator+irequest);
			//初始化context
			initContext();
			//执行job
			response = execute(itype,request);
			writeJson(response,dir+"response"+File.separator+iresponse);
			System.out.println(response);
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
}
