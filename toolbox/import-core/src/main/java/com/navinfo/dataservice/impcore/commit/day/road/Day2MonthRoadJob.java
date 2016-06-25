package com.navinfo.dataservice.impcore.commit.day.road;

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
 * 道路数据从日大区库落入月大区库
 */
public class Day2MonthRoadJob extends AbstractJob {
	public Day2MonthRoadJob(JobInfo jobInfo) {
		super(jobInfo);
	}
	
	public void execute()
			throws JobException {
		try{
			this.log.info("获取大区和grid的映射关系");
			List<Region> regionsWithGrids= queryRegionGridsMapping();
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Region regionInfo:regionsWithGrids){
				List<Integer> gridListOfRegion = regionInfo.getGrids();
				DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
				DbInfo dailyDb = databhubApi.getDbById(regionInfo.getDailyDbId());
				DbInfo monthlyDb = databhubApi.getDbById(regionInfo.getMonthlyDbId());
				this.log.info("开始进行日落月（源库:"+dailyDb+",目标库："+monthlyDb+")");
				LogFlusher logFlusher= new Day2MonRoadLogFlusher(regionInfo.getRegionId(),
													dailyDb, 
													monthlyDb, 
													gridListOfRegion, 
													((Day2MonthRoadJobRequest )this.request).getStopTime());
				logFlusher.setLog(this.log);
				FlushResult result= logFlusher.perform();
				jobResponse.put(regionInfo.getRegionId().toString(), result);
			}
			this.response("日落月执行完毕", jobResponse);
			
		}catch(Exception e){
			throw new JobException(e);
		}
	}

	private List<Region> queryRegionGridsMapping() throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionWithGridsList= manApi.queryRegionWithGrids(((Day2MonthRoadJobRequest )this.request).getGridList());
		return regionWithGridsList;
	}

	
	

}

