package com.navinfo.dataservice.scripts;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/**根据城市进行poi的日落月
 * @ClassName: Day2Mon
 * @author MaYunFei
 * @date 下午5:18:18
 * @Description: Day2Mon.java
 */
public class Day2Mon {

	public static JSONObject execute(JSONObject request) throws Exception{
		JobInfo jobInfo = new JobInfo(0,UuidUtils.genUuid());
		jobInfo.setType("day2MonSync");
		jobInfo.setRequest(request);
		jobInfo.setTaskId(0);
		AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
		job.run();
		return job.getJobInfo().getResponse();
	}
	public static void main(String[] args) throws Exception{
		initContext();
		JSONObject request = new JSONObject();
		request.put("cityId", null);
		JSONObject response = execute(request);
		System.out.println(response);
		System.out.println("Over.");
		System.exit(0);
	}
	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	


}
