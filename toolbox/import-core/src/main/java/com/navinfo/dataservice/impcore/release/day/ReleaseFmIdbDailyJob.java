package com.navinfo.dataservice.impcore.release.day;

import java.util.HashMap;
import java.util.List;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
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
	public void execute() throws JobException {
		LogSelector logSelector =null;
		int produceId=0;
		boolean commitStatus=false;
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		try{
			ReleaseFmIdbDailyJobRequest releaseFmIdbDailyRequest = (ReleaseFmIdbDailyJobRequest )this.request;
			produceId=releaseFmIdbDailyRequest.getProduceId();
			//日出品状态修改为 进行中
			manApi.updateProduceStatus(produceId, 1);
			List<Region> regionsWithGrids= queryRegionGridsMapping();
			HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
			DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
			String featureType = releaseFmIdbDailyRequest.getFeatureType();
			for (Region regionInfo:regionsWithGrids){
				this.log.info("regionInfo:"+regionInfo);
				List<Integer> gridListOfRegion = regionInfo.getGrids();
				try{
					DbInfo dailyDb = databhubApi.getDbById(regionInfo.getDailyDbId());
					OracleSchema srcDbSchema = new OracleSchema(
							DbConnectConfig.createConnectConfig(dailyDb.getConnectParam()));
					logSelector = createLogSelector(featureType,srcDbSchema,gridListOfRegion);
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
				
			}
			this.log.info("调用出品转换api");
			callReleaseTransApi();
			this.response("日出品执行完毕", jobResponse);
			//日出品状态修改为 完成
			manApi.updateProduceStatus(produceId, 2);
			
		}catch(Exception e){
			//日出品状态修改为 失败
			try {
				manApi.updateProduceStatus(produceId, 3);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				throw new JobException(e1);
			}
			throw new JobException(e);
		}finally{
			
		}
		

	}
	private LogStatusModifier createLogStatusModifier(String featureType,
			OracleSchema srcDbSchema, String tempTable) {
		if ("POI".equals(featureType)){
			return new PoiReleaseDailyLogStatusModifier(srcDbSchema,tempTable);
		}
		return new DefaultDailyLogStatusModifier(srcDbSchema,tempTable);
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

	
	

	private void callReleaseTransApi() {
		// TODO 待实现；
	}

	private List<Region> queryRegionGridsMapping() throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionWithGridsList= manApi.queryRegionWithGrids(((ReleaseFmIdbDailyJobRequest )this.request).getGridList());
		return regionWithGridsList;
	}
	
	private LogSelector createLogSelector(String featureType,OracleSchema logSchema,List<Integer> grids){
		if ("POI".equals(featureType)){
			return new PoiDailyReleaseLogSelector(logSchema,grids);
		}
		return new DeafultDailyReleaseLogSelector(logSchema,grids);
		
		
	}
	private DbInfo getReleaseDbConn(DatahubApi databhubApi,String featureType) throws Exception {
		if ("POI".equals(featureType)){
			return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_POI.getValue());
		}
		return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_ALL.getValue());
	}

	

}

