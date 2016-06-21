package com.navinfo.dataservice.impcore.release.day;

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
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-coreReleaseFmIdbDailyJob.java
 */
public class ReleaseFmIdbDailyJob extends AbstractJob {

	public ReleaseFmIdbDailyJob(JobInfo jobInfo) {
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
				ReleaseFmIdbDailyJobRequest releaseFmIdbDailyRequest = (ReleaseFmIdbDailyJobRequest )this.request;
				DbInfo releaseDb = getReleaseDbConn(databhubApi,releaseFmIdbDailyRequest.getFeatureType());//databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_ALL.getValue());
				this.log.info("开始日出品（源库:"+dailyDb+",目标库："+releaseDb+")");
				LogFlusher logFlusher= new ReleaseDailyLogFlusher(regionInfo.getRegionId(),
													dailyDb, 
													releaseDb, 
													gridListOfRegion, 
													releaseFmIdbDailyRequest.getStopTime(),
													releaseFmIdbDailyRequest.getFeatureType()//LogFlusher.FEATURE_POI
													);
				logFlusher.setLog(this.log);
				FlushResult result= logFlusher.perform();//刷库
				this.log.info("调用出品转换api");
				callReleaseTransApi();
				jobResponse.put(regionId.toString(), result);
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

	/**
	 * @return 大区id和对应grid的mapping信息
	 * @throws Exception
	 */
	private Map queryRegionGridsMapping() throws Exception {
		ManApi gridSelectorApiSvr = (ManApi) ApplicationContextUtil.getBean("manApi");
		Map regionGridMapping= gridSelectorApiSvr.queryRegionGridMapping(((ReleaseFmIdbDailyJobRequest )this.request).getGridList());
		return regionGridMapping;
	}

}

