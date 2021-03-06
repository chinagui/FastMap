package com.navinfo.dataservice.impcore.release.mon;

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
import com.navinfo.dataservice.impcore.commit.mon.CommitMonthlyLogFlusher;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
import com.navinfo.dataservice.impcore.release.day.ReleaseFmIdbDailyJobRequest;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/*
 * 从GDB+母库 进行月出品
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-coreReleaseFmIdbDailyJob.java
 */
public class ReleaseFmIdbMonthlyJob extends AbstractJob {

	public ReleaseFmIdbMonthlyJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException  {
		try{
				HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
				this.log.info("得到母库信息");
				DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
				DbInfo gdbPlus = databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS.getValue());
				ReleaseFmIdbMonthlyJobRequest req = (ReleaseFmIdbMonthlyJobRequest )this.request;
				List<Region> regionsWithGrids= LogFlushUtil.getInstance().queryRegionGridsMapping(req.getGridList());
				for (Region regionInfo :regionsWithGrids){
					this.log.info("得到大区对应的grid列表");
					List<Integer> gridListOfRegion = regionInfo.getGrids();
					DbInfo monthlyDb = databhubApi.getDbById(regionInfo.getMonthlyDbId());
					this.log.info("开始进行月落GDB+库（源库:"+monthlyDb+",目标库："+gdbPlus+")");
					LogFlusher logFlusher= new CommitMonthlyLogFlusher(regionInfo.getRegionId(),
														monthlyDb, 
														gdbPlus, 
														gridListOfRegion, 
														req.getStopTime());
					logFlusher.setLog(this.log);
					FlushResult result= logFlusher.perform();
					jobResponse.put(regionInfo.getRegionId().toString(), result);
				}
				this.response("月落大区执行完毕", jobResponse);
				
				DbInfo releaseDb =  databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_MON.getValue());
				this.log.info("开始月出品（源库:"+gdbPlus+",目标库："+releaseDb+")");
				LogFlusher logFlusher= new ReleaseMonthlyLogFlusher(
													gdbPlus, 
													releaseDb, 
													req.getGridList(), 
													req.getStopTime()
													);
				logFlusher.setLog(this.log);
				FlushResult result= logFlusher.perform();//刷库
				this.log.info("调用出品转换api");
				callReleaseTransApi();
				jobResponse.put("monthlyRelease", result);
				this.response("月出品执行完毕", jobResponse);
			
		}catch(Exception e){
			throw new JobException(e);
		}
		

	}
	private void callReleaseTransApi() {
		// TODO 待实现；
	}

	
}

