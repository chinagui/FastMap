package com.navinfo.dataservice.impcore.commit.mon;

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
import com.navinfo.dataservice.impcore.commit.day.poi.Day2MonPoiLogFlusher;
import com.navinfo.dataservice.impcore.commit.day.road.Day2MonRoadLogFlusher;
import com.navinfo.dataservice.impcore.commit.day.road.Day2MonthRoadJobRequest;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
import com.navinfo.dataservice.impcore.release.day.ReleaseFmIdbDailyJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/*
 * 月库提交：从月库提交数据到GDB+
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-CommitMonthlyJob.java
 */
public class CommitMonthlyJob extends AbstractJob {

	public CommitMonthlyJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException  {
		try{
			this.log.info("获取大区和grid的映射关系");
			Map regionGridMapping = queryRegionGridsMapping();
			Set<Integer> regionSet = regionGridMapping.keySet();
			this.log.debug("regionSet:"+regionSet);
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Integer regionId:regionSet){
				this.log.info("得到大区对应的grid列表");
				List<Integer> gridListOfRegion = (List<Integer>) regionGridMapping.get(regionId);
				//根据大区id获取对应的大区月库
				ManApi manApi =  (ManApi) ApplicationContextUtil.getBean("manApi");
				Region regionInfo = manApi.queryByRegionId(regionId);
				if (regionInfo==null){
					this.log.warn("根据regionId："+regionId+",没有得到大区库数据");
					continue;
				}
				DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
				DbInfo monthlyDb = databhubApi.getDbById(regionInfo.getMonthlyDbId());
				DbInfo gdbPlus = databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS.getValue());
				this.log.info("开始进行日落月（源库:"+monthlyDb+",目标库："+gdbPlus+")");
				LogFlusher logFlusher= new CommitMonthlyLogFlusher(regionInfo.getRegionId(),
													monthlyDb, 
													gdbPlus, 
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
	private void callReleaseTransApi() {
		// TODO 待实现；
	}


	/**
	 * @return 大区id和对应grid的mapping信息
	 * @throws Exception
	 */
	private Map queryRegionGridsMapping() throws Exception {
		ManApi gridSelectorApiSvr = (ManApi) ApplicationContextUtil.getBean("manApi");
		Map regionGridMapping= gridSelectorApiSvr.queryRegionGridMapping(((CommitMonthlyJobRequest )this.request).getGridList());
		return regionGridMapping;
	}

}

