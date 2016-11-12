package com.navinfo.dataservice.scripts;

import java.util.List;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import net.sf.json.JSONObject;


/** 
 * @ClassName: ReleaseFmIdbDailyPoi
 * @author songdongyan
 * @date 2016年11月10日
 * @Description: ReleaseFmIdbDailyPoi.java
 */
public class ReleaseFmIdbDailyPoi {

	/**
	 * 
	 */
	public ReleaseFmIdbDailyPoi() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private static List<Region> queryRegionList() throws Exception {
		// TODO Auto-generated method stub
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionList= manApi.queryRegionList();
		return regionList;
	}
	
	public static void main(String[] args){
		JobScriptsInterface.initContext();
		try{
			JSONObject paraJson = null;
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			//获取所有大区库信息
			List<Region> regionList = queryRegionList();
			for (Region regionInfo:regionList){
				int produceId = manApi.createJob(0,"POI",paraJson);
				//创建日出品job
				// TODO Auto-generated method stub
				JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
				//TODO
				JSONObject jobDataJson=new JSONObject();
				jobDataJson.put("produceId", produceId);
				jobDataJson.put("featureType", "POI");
				jobDataJson.put("regionId", regionInfo.getRegionId());
				long jobId=jobApi.createJob("releaseFmIdbDailyPoiJob", jobDataJson, 0, "日出品");
				System.out.println("jobId" + jobId);
			}
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
}
