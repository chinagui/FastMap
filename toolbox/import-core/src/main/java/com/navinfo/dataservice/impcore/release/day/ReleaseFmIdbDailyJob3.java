package com.navinfo.dataservice.impcore.release.day;

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
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-coreReleaseFmIdbDailyJob.java
 */
public class ReleaseFmIdbDailyJob3 extends AbstractJob {

	public ReleaseFmIdbDailyJob3(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException  {
		try{
			List<Region> regionsWithGrids= queryRegionGridsMapping();
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			for (Region regionInfo:regionsWithGrids){
				List<Integer> gridListOfRegion = regionInfo.getGrids();
				DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
				DbInfo dailyDb = databhubApi.getDbById(regionInfo.getDailyDbId());
				ReleaseFmIdbDailyJobRequest releaseFmIdbDailyRequest = (ReleaseFmIdbDailyJobRequest )this.request;
				DbInfo releaseDb = getReleaseDbConn(databhubApi,releaseFmIdbDailyRequest.getFeatureType());//databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_ALL.getValue());
				this.log.info("开始日出品（源库:"+dailyDb+",目标库："+releaseDb+")");
				LogFlusher logFlusher= new ReleaseDailyLogFlusher(regionInfo.getRegionId(),
													dailyDb, 
													releaseDb, 
													gridListOfRegion, 
													releaseFmIdbDailyRequest.getFeatureType()//LogFlusher.FEATURE_POI
													);
				logFlusher.setLog(this.log);
				FlushResult result= logFlusher.perform();//刷库
				this.log.info("调用出品转换api");
				callReleaseTransApi();
				jobResponse.put(regionInfo.getRegionId().toString(), result);
			}
			this.response("日出品执行完毕", jobResponse);
			
		}catch(Exception e){
			throw new JobException(e);
		}
		

	}
	

	private void callReleaseTransApi() {
		// TODO 待实现；
	}

	/**
	 * @param databhubApi
	 * @param featureType
	 * @return 出品库的数据库连接信息
	 * @throws Exception
	 */
	private DbInfo getReleaseDbConn(DatahubApi databhubApi,String featureType) throws Exception {
		if (LogFlusher.FEATURE_POI.equals(featureType)){
			return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_POI.getValue());
		}
		return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_ALL.getValue());
	}

	private List<Region> queryRegionGridsMapping() throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionWithGridsList= manApi.queryRegionWithGrids(((ReleaseFmIdbDailyJobRequest )this.request).getGridList());
		return regionWithGridsList;
	}

}

