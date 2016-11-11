package com.navinfo.dataservice.impcore.release.day.poi;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
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

	public static void main(String[] args){
		try{
			JSONObject paraJson = null;
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			int produceId = manApi.createJob(0,"POI",paraJson);
			//创建日出品job
			// TODO Auto-generated method stub
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			/*
			 * {"gridIds":[213424,343434,23423432],"stopTime":"yyyymmddhh24miss","dataType":"POI"//POI,ALL}
			 * jobType:releaseFmIdbDailyJob/releaseFmIdbMonthlyJob
			 */
			//TODO
			JSONObject jobDataJson=new JSONObject();
			jobDataJson.put("produceId", produceId);
			jobDataJson.put("featureType", "POI");
			long jobId=jobApi.createJob("releaseFmIdbDailyPoiJob", jobDataJson, 0, "日出品");
			System.out.println("jobId" + jobId);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}
	
}
