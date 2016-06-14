package com.navinfo.dataservice.impcore.commit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.IRegion;
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
public class CommitDay2MonthRoadJob extends AbstractJob {

	public CommitDay2MonthRoadJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	/* 给定grid列表；
	 * 根据grid计算出对应的大区库，并将对应大区日库中grid范围内的履历刷到月库；
	 * 刷履历过程中，如果出现异常，需要跳过异常继续刷其他的履历；
	 * 出现异常的grid需要给grid打标记为"日落月失败"；
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		CommitDay2MonthRoadJobRequest req = (CommitDay2MonthRoadJobRequest)this.getRequest();
		List<Integer> gridList = req.getGridList();
		String stopTime = req.getStopTime();
		ManApi gridSelectorApiSvr = (ManApi) ApplicationContextUtil.getBean("manApi");
		try{
			this.log.info("获取大区和grid的映射关系");
			Map regionGridMapping = gridSelectorApiSvr.queryRegionGridMapping(gridList);
			Set<Integer> regionSet = regionGridMapping.keySet();
			this.log.debug("regionSet:"+regionSet);
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Integer regionId:regionSet){
				this.log.info("得到大区对应的grid列表");
				List<Integer> gridListOfRegion = (List<Integer>) regionGridMapping.get(regionId);
				//在大区日库中根据grid列表获取履历，并刷新对应的月库
				//根据大区id获取对应的大区日库、大区月库
				ManApi manApi =  (ManApi) ApplicationContextUtil.getBean("manApi");
				IRegion regionDbInfo = manApi.queryByRegionId(regionId);
				DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
				DbInfo dailyDb = databhubApi.getDbById(regionDbInfo.getDailyDbId());
				DbInfo monthlyDb = databhubApi.getDbById(regionDbInfo.getMonthlyDbId());
				this.log.info("开始进行日落月（源库:"+dailyDb+",目标库："+monthlyDb+")");
				LogFlusher logFlusher= new LogFlusher(dailyDb, monthlyDb, gridListOfRegion, req.getStopTime(),LogFlusher.FEATURE_ROAD);
				logFlusher.setLog(this.log);
				FlushResult result= logFlusher.perform();
				jobResponse.put(regionId.toString(), result);
			}
			this.response("日落月执行完毕", jobResponse);
			
		}catch(Exception e){
			throw new JobException(e);
		}
		
		
	}
	public class FlushResultWrap {
		FlushResult result ;
		Integer regionId ;
		public FlushResultWrap(FlushResult result, Integer regionId) {
			super();
			this.result = result;
			this.regionId = regionId;
		}
		
		
	}

}

