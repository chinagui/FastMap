package com.navinfo.dataservice.impcore.release.day.poi;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.DefaultLogFlusher;
import com.navinfo.dataservice.impcore.flusher.LogFlusher;
import com.navinfo.dataservice.impcore.mover.DefaultLogMover;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.impcore.selector.DeafultDailyReleaseLogSelector;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import com.navinfo.dataservice.impcore.selector.PoiDailyReleaseLogSelector;
import com.navinfo.dataservice.impcore.statusModifier.DefaultDailyLogStatusModifier;
import com.navinfo.dataservice.impcore.statusModifier.LogStatusModifier;
import com.navinfo.dataservice.impcore.statusModifier.PoiReleaseDailyLogStatusModifier;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
 * @ClassName: ReleaseFmIdbDailyPoiJob
 * @author songdongyan
 * @date 2016年11月10日
 * @Description: ReleaseFmIdbDailyPoiJob.java
 */
public class ReleaseFmIdbDailyPoiJob extends AbstractJob {

	/**
	 * @param jobInfo
	 */
	public ReleaseFmIdbDailyPoiJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException  {
		LogSelector logSelector =null;
		boolean commitStatus=false;
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
		try{
			ReleaseFmIdbDailyPoiJobRequest releaseFmIdbDailyPoiRequest = (ReleaseFmIdbDailyPoiJobRequest )this.request;
			int produceId=releaseFmIdbDailyPoiRequest.getProduceId();
			//日出品状态修改为 进行中
			manApi.updateProduceStatus(produceId, 1);
//			//获取所有大区库信息
//			List<Region> regionList = queryRegionList();
			int regionId = releaseFmIdbDailyPoiRequest.getRegionId();
			Region regionInfo = manApi.queryByRegionId(regionId);
			
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();

			String featureType = releaseFmIdbDailyPoiRequest.getFeatureType();
			this.log.info("regionInfo:"+regionInfo);
			try{
				//履历删选
				DbInfo dailyDb = databhubApi.getDbById(regionInfo.getDailyDbId());
				OracleSchema srcDbSchema = new OracleSchema(
						DbConnectConfig.createConnectConfig(dailyDb.getConnectParam()));
				logSelector = createLogSelector(featureType,srcDbSchema,null);
				String tempTable = logSelector.select();
				this.log.info("履历选择完成,srcDb:"+dailyDb);
				//履历刷库；
				DbInfo releaseDb = getReleaseDbConn(databhubApi, featureType);
				OracleSchema targetDbSchema = new OracleSchema(
						DbConnectConfig.createConnectConfig(releaseDb.getConnectParam()));
				LogFlusher logFlusher = new DefaultLogFlusher(srcDbSchema, targetDbSchema, false, tempTable);
				FlushResult result = logFlusher.flush();
				response("",null);
				this.log.info("履历刷库完成,targetDb:"+releaseDb);
				//履历搬迁
				LogMover logMover = new DefaultLogMover(srcDbSchema, targetDbSchema, tempTable, null);
				logMover.move();
				this.log.info("履历搬迁完成,targetDb:"+releaseDb);
				//更新状态	
				LogStatusModifier logStatusModifier = createLogStatusModifier(featureType,srcDbSchema,tempTable);//new PoiReleaseDailyLogStatusModifier(srcDbSchema,tempTable);
				logStatusModifier.execute();
				this.log.info("完成出品状态更新");
				jobResponse.put(regionInfo.getRegionId().toString(), result);
				commitStatus=true;
			}catch(Exception e){
				//日出品状态修改为 失败
				manApi.updateProduceStatus(produceId, 3);
				throw new JobException(e);
			}
			finally{
				unselectLog(logSelector,commitStatus);
			}

//			for (Region regionInfo:regionList){
//				this.log.info("regionInfo:"+regionInfo);
//				try{
//					//履历删选
//					DbInfo dailyDb = databhubApi.getDbById(regionInfo.getDailyDbId());
//					OracleSchema srcDbSchema = new OracleSchema(
//							DbConnectConfig.createConnectConfig(dailyDb.getConnectParam()));
//					logSelector = createLogSelector(featureType,srcDbSchema,null);
//					String tempTable = logSelector.select();
//					this.log.info("履历选择完成,srcDb:"+dailyDb);
//					//履历刷库；
//					DbInfo releaseDb = getReleaseDbConn(databhubApi, featureType);
//					OracleSchema targetDbSchema = new OracleSchema(
//							DbConnectConfig.createConnectConfig(releaseDb.getConnectParam()));
//					LogFlusher logFlusher = new DefaultLogFlusher(srcDbSchema, targetDbSchema, false, tempTable);
//					FlushResult result = logFlusher.flush();
//					response("",null);
//					this.log.info("履历刷库完成,targetDb:"+releaseDb);
//					//履历搬迁
//					LogMover logMover = new DefaultLogMover(srcDbSchema, targetDbSchema, tempTable, null);
//					logMover.move();
//					this.log.info("履历搬迁完成,targetDb:"+releaseDb);
//					//更新状态	
//					LogStatusModifier logStatusModifier = createLogStatusModifier(featureType,srcDbSchema,tempTable);//new PoiReleaseDailyLogStatusModifier(srcDbSchema,tempTable);
//					logStatusModifier.execute();
//					this.log.info("完成出品状态更新");
//					jobResponse.put(regionInfo.getRegionId().toString(), result);
//					commitStatus=true;
//				}catch(Exception e){
//					//日出品状态修改为 失败
//					manApi.updateProduceStatus(produceId, 3);
//					throw new JobException(e);
//				}
//				finally{
//					unselectLog(logSelector,commitStatus);
//				}
//				
//			}
			this.log.info("调用出品转换api");
			callReleaseTransApi();
			this.response("日出品执行完毕", jobResponse);
			//日出品状态修改为 完成
			manApi.updateProduceStatus(produceId, 2);
			
		}catch(Exception e){
			throw new JobException(e);
		}finally{
			
		}	
	}
	
	/**
	 * @return
	 * @throws Exception 
	 */
	private List<Region> queryRegionList() throws Exception {
		// TODO Auto-generated method stub
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionList= manApi.queryRegionList();
		return regionList;
	}

	private void callReleaseTransApi() {
		// TODO 待实现；
	}
	
	private void unselectLog(LogSelector logSelector, boolean commitStatus) {
		if(logSelector!=null){
			try{
				logSelector.unselect(commitStatus);
			}catch(Exception e){
				log.warn("履历重置状态时发生错误，请手工对应。"+e.getMessage(),e);
			}
		}	
	}

	
	private LogSelector createLogSelector(String featureType,OracleSchema logSchema,List<Integer> grids){
		if ("POI".equals(featureType)){
			return new PoiDailyReleaseLogSelector(logSchema,grids);
		}
		return new DeafultDailyReleaseLogSelector(logSchema,grids);	
	}
	
	private LogStatusModifier createLogStatusModifier(String featureType,
			OracleSchema srcDbSchema, String tempTable) {
		if ("POI".equals(featureType)){
			return new PoiReleaseDailyLogStatusModifier(srcDbSchema,tempTable);
		}
		return new DefaultDailyLogStatusModifier(srcDbSchema,tempTable);
	}
	
	private DbInfo getReleaseDbConn(DatahubApi databhubApi,String featureType) throws Exception {
		if ("POI".equals(featureType)){
			return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_POI.getValue());
		}
		return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_ALL.getValue());
	}
	
}
