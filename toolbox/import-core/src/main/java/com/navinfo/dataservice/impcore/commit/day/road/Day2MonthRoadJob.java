package com.navinfo.dataservice.impcore.commit.day.road;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			Map regionGridMapping = queryRegionGridsMapping();
			Set<Integer> regionSet = regionGridMapping.keySet();
			this.log.debug("regionSet:"+regionSet);
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Integer regionId:regionSet){
				this.log.info("得到大区对应的grid列表");
				List<Integer> gridListOfRegion = (List<Integer>) regionGridMapping.get(regionId);
				//在大区日库中根据grid列表获取履历，并刷新对应的月库
				//根据大区id获取对应的大区日库、大区月库
				ManApi manApi =  (ManApi) ApplicationContextUtil.getBean("manApi");
				Region regionInfo = manApi.queryByRegionId(regionId);
				if (regionInfo==null){
					this.log.warn("根据regionId："+regionId+",没有得到大区库数据");
					continue;
				}
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
				jobResponse.put(regionId.toString(), result);
			}
			this.response("日落月执行完毕", jobResponse);
			
		}catch(Exception e){
			throw new JobException(e);
		}
	}

	private Map queryRegionGridsMapping() throws Exception {
		ManApi gridSelectorApiSvr = (ManApi) ApplicationContextUtil.getBean("manApi");
		Map regionGridMapping= gridSelectorApiSvr.queryRegionGridMapping(((Day2MonthRoadJobRequest )this.request).getGridList());
		return regionGridMapping;
	}

	
	

}

