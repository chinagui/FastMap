package com.navinfo.dataservice.scripts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.iface.FmMultiSrcSyncApi;
import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

public class Fm2MultiSrcSyncScript {

	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		try {
			JSONObject job = new JSONObject();
			FmMultiSrcSyncApi syncApi = (FmMultiSrcSyncApi) ApplicationContextUtil.getBean("fmMultiSrcSyncApi");
			//查询最近的成功同步时间
			FmMultiSrcSync fmMultiSrcSync = syncApi.queryLastSuccessSync();
			String lastSyncTime = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			if(fmMultiSrcSync.getSyncTime() != null){
				lastSyncTime = sdf.format(fmMultiSrcSync.getSyncTime());
			}
			job.put("lastSyncTime", lastSyncTime);
			//ͬ同步时间
			String syncTime = sdf.format(new Date());
			job.put("syncTime", syncTime);
			//查询dbIds
			List<Region> regionList = RegionService.getInstance().list();
			List<Integer> dbIds = new ArrayList<Integer>();
			for (Region region : regionList) {
				if(region.getDailyDbId() != null){
					dbIds.add(region.getDailyDbId());
				}
			}
			job.put("dbIds", dbIds);
			//创建job,获取jobId
			long jobId = JobService.getInstance().create("fm2MultiSrcSync", job, 0, "创建FM日库多源增量包");
			//创建管理记录
			syncApi.insertFmMultiSrcSync(jobId,syncTime);
			response.put("msg", "执行成功");
		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		JSONObject request=null;
		JSONObject response = null;
		try {
			response = execute(request);
			System.out.println(response);
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
