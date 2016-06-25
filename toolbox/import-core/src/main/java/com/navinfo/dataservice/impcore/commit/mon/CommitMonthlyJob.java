package com.navinfo.dataservice.impcore.commit.mon;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.commit.day.road.Day2MonthRoadJobRequest;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
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
			List<Region> regionsWithGrids= queryRegionGridsMapping();
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Region regionInfo :regionsWithGrids){
				this.log.info("得到大区对应的grid列表");
				List<Integer> gridListOfRegion = regionInfo.getGrids();
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
				jobResponse.put(regionInfo.getRegionId().toString(), result);
			}
			this.response("日落月执行完毕", jobResponse);
		}catch(Exception e){
			throw new JobException(e);
		}
		

	}

	/**
	 * @return 大区id和对应grid的mapping信息
	 * @throws Exception
	 */
	private List<Region> queryRegionGridsMapping() throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionWithGridsList= manApi.queryRegionWithGrids(((Day2MonthRoadJobRequest )this.request).getGridList());
		return regionWithGridsList;
	}

}

