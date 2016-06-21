package com.navinfo.dataservice.impcore.commit.day.poi;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：job-frameworkCommitDay2MonthRoad.java
 * POI数据从日大区库落入月大区库
 */
public class Day2MonthPoiJob extends AbstractJob {

	public Day2MonthPoiJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException{
		try{
			this.log.info("获取大区列表");
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			List<Region> regionSet = manApi.queryRegionList();
			this.log.debug("regionSet:"+regionSet);
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Region region:regionSet){
				DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
				DbInfo dailyDb = databhubApi.getDbById(region.getDailyDbId());
				DbInfo monthlyDb = databhubApi.getDbById(region.getMonthlyDbId());
				this.log.info("开始进行日落月（源库:"+dailyDb+",目标库："+monthlyDb+")");
				LogFlusher logFlusher= new Day2MonPoiLogFlusher(region.getRegionId(),
													dailyDb, 
													monthlyDb);
				logFlusher.setLog(this.log);
				FlushResult result= logFlusher.perform();
				jobResponse.put(region.getRegionId().toString(), result);
			}
			afterFlush();//TODO:实现落入月库的poi数据的处理：批处理、检查生成精编作业项，从而可以进行精编作业。
			this.response("日落月执行完毕", jobResponse);
			
		}catch(Exception e){
			throw new JobException(e);
		}
	}
	private void afterFlush(){
		//TODO:实现落入月库的poi数据的处理：批处理、检查生成精编作业项，从而可以进行精编作业。
	}

}

