package com.navinfo.dataservice.scripts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.iface.SyncApi;
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
			SyncApi syncApi = (SyncApi) ApplicationContextUtil.getBean("syncApi");
			//��ѯ�ϴ�ͬ���ɹ���ʱ��
			FmMultiSrcSync fmMultiSrcSync = syncApi.queryLastSuccessSync();
			String lastSyncTime = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			if(fmMultiSrcSync.getSyncTime() != null){
				lastSyncTime = sdf.format(fmMultiSrcSync.getSyncTime());
			}
			job.put("lastSyncTime", lastSyncTime);
			//ͬ��ʱ��
			String syncTime = sdf.format(new Date());
			job.put("syncTime", syncTime);
			//��ѯ������dbIds
			List<Region> regionList = RegionService.getInstance().list();
			List<Integer> dbIds = new ArrayList<Integer>();
			for (Region region : regionList) {
				if(region.getDailyDbId() != null){
					dbIds.add(region.getDailyDbId());
				}
			}
			job.put("dbIds", dbIds);
			//����job����,��ȡjobId
			long jobId = JobService.getInstance().create("fm2MultiSrcSync", job, 0, "����FM�տ��Դ������");
			//���������¼
			syncApi.insertFmMultiSrcSync(jobId,syncTime);
			response.put("msg", "ִ�гɹ�");
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
