package com.navinfo.dataservice.impcore.commit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：job-frameworkCommitDay2MonthRoad.java
 * POI数据从日大区库落入月大区库
 */
public class CommitDay2MonthPoiJob extends AbstractCommitDay2MonthJob {

	public CommitDay2MonthPoiJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public IDay2MonthCommand createDay2MonthCommand() {
		return new CommitDay2MonthPoiCommand(this.request);
	}
	@Override
	public void doFlushFromDay2Month(IDay2MonthCommand command){
		try{
			this.log.info("获取大区和grid的映射关系");
			ManApi gridSelectorApiSvr = (ManApi) ApplicationContextUtil.getBean("manApi");
			List<Region> regionSet = gridSelectorApiSvr.queryRegionList();
			this.log.debug("regionSet:"+regionSet);
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Region region:regionSet){
				DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
				DbInfo dailyDb = databhubApi.getDbById(region.getDailyDbId());
				DbInfo monthlyDb = databhubApi.getDbById(region.getMonthlyDbId());
				this.log.info("开始进行日落月（源库:"+dailyDb+",目标库："+monthlyDb+")");
				LogFlusher logFlusher= new LogFlusher(region.getRegionId(),
													dailyDb, 
													monthlyDb, 
													gridListOfRegion, 
													command.getStopTime(),
													command.getFlushFeatureType(),
													command.getLockType());
				logFlusher.setLog(this.log);
				FlushResult result= logFlusher.perform();
				jobResponse.put(regionId.toString(), result);
			}
			this.response("日落月执行完毕", jobResponse);
			
		}catch(Exception e){
			throw new JobException(e);
		}
	}
	}
	@Override
	public void afterFlush(){
		//TODO:实现落入月库的poi数据的处理：批处理、检查生成精编作业项，从而可以进行精编作业。
	}
	public class CommitDay2MonthPoiCommand implements IDay2MonthCommand{
		AbstractJobRequest req;
		
		public CommitDay2MonthPoiCommand(AbstractJobRequest req) {
			super();
			this.req = req;
		}

		@Override
		public Map queryRegionGridMapping() throws Exception {
			ManApi gridSelectorApiSvr = (ManApi) ApplicationContextUtil.getBean("manApi");
			return gridSelectorApiSvr.queryRegionGridMappingOfSubtasks(((CommitDay2MonthPoiJobRequest )req).getTaskId());
		}

		@Override
		public String getStopTime() {
			return null;//poi日落月没有stoptime
		}

		@Override
		public String getFlushFeatureType() {
			return LogFlusher.FEATURE_POI;
		}

		@Override
		public int getLockType() {
			return FmEditLock.TYPE_DAY2MON;
		}
		
	}
	

}

