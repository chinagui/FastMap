package com.navinfo.dataservice.column.job;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.Day2MonthSyncApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationResultException;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.day2mon.Check;
import com.navinfo.dataservice.day2mon.Classifier;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogSelector;
import com.navinfo.dataservice.day2mon.PostBatch;
import com.navinfo.dataservice.day2mon.PreBatch;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogFlusher;
import com.navinfo.dataservice.impcore.mover.Day2MonMover;
import com.navinfo.dataservice.impcore.mover.LogMoveResult;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
 * @ClassName: Day2MonthPoiMergeJob
 * @author MaYunFei
 * @date 下午8:27:56
 * @Description: POI 日落月融合Job
 * 1.找到那些开关属于打开状态的城市
 * 2.按照大区将这些城市进行分组；key=cityid，value=regionid;
 * 3.按city开始找满足条件的履历
 * 4.分析履历，增加、修改的poi要放到OperationResult 的列表中，为后续的精编批处理、检查做准备
 * 5.将3得到的履历刷到月库；
 * 6.搬履历到月库；
 * 7.月库执行精编批处理检查：根据4得到的OperationResult
 * 8.月库打重分类的标记
 * 9.深度信息打标记；
 * 10.修改day_mon_sync 状态为成功
 * 11.按照城市统计日落月的数据量
 */
public class Day2MonthPoiMergeJob extends AbstractJob {

	public Day2MonthPoiMergeJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		ManApi manApi = (ManApi)ApplicationContextUtil
				.getBean("manApi");
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil
				.getBean("datahubApi");
		Day2MonthSyncApi d2mSyncApi = (Day2MonthSyncApi)ApplicationContextUtil
				.getBean("day2MonthSyncApi");
		try {
			log.info("开始获取日落月开关控制信息");
			Set<Integer> statusValues = new HashSet<Integer>();
			statusValues.add(0);
			JSONObject conditionJson = new JSONObject().element("status", statusValues);
			List<Map<String, Object>> d2mInfoList= manApi.queryDay2MonthList(conditionJson );
			response("获取日落月开关控制信息ok",null);
			log.info("开始获取日落月城市信息:城市的基础信息、城市的grid，城市的大区库");
			Day2MonthPoiMergeJobRequest day2MonRequest=(Day2MonthPoiMergeJobRequest) request;
			String reqCityId = day2MonRequest==null?null:day2MonRequest.getCityId();
			if(StringUtils.isNotEmpty(reqCityId)){
				doSync(manApi, datahubApi, d2mSyncApi, Integer.parseInt(reqCityId));
			}else{//全部DAY2MONTH_CONFIG中处于打开状态的城市
				for(Map<String,Object> d2mInfo:d2mInfoList){
					Integer cityId = (Integer) d2mInfo.get("cityId");
					doSync(manApi, datahubApi, d2mSyncApi, cityId);
				}
			}
			
			log.info("日落月完成");
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
			
		}
		

	}

	private void doSync(ManApi manApi, DatahubApi datahubApi, Day2MonthSyncApi d2mSyncApi, Integer cityId)
			throws Exception{
		Map<String, Object> cityInfo = manApi.getCityById(cityId);
		log.info("得到城市基础信息:"+cityInfo);
		List<Integer> gridsOfCity = manApi.queryGridOfCity(cityId);
		log.info("得到城市的grids");
		Integer regionId = (Integer) cityInfo.get("regionId");
		Region regionInfo = manApi.queryByRegionId(regionId);
		log.info("获取大区信息:"+regionInfo);
		if(regionInfo==null) return ;
		Integer dailyDbId = regionInfo.getDailyDbId();
		DbInfo dailyDbInfo = datahubApi.getDbById(dailyDbId);
		log.info("获取dailyDbInfo信息:"+dailyDbInfo);
		Integer monthDbId = regionInfo.getMonthlyDbId();
		DbInfo monthDbInfo = datahubApi.getDbById(monthDbId);
		log.info("获取monthDbInfo信息:"+monthDbInfo);
		FmDay2MonSync lastSyncInfo = d2mSyncApi.queryLastedSyncInfo(cityId);
		log.info("获取最新的成功同步信息："+lastSyncInfo);				
		Date syncTimeStamp= new Date();
		FmDay2MonSync curSyncInfo = createSyncInfo(d2mSyncApi, cityId,syncTimeStamp);//记录本次的同步信息
		d2mSyncApi.insertSyncInfo(curSyncInfo);
		Day2MonPoiLogSelector logSelector = null;
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo.getConnectParam()));
		Connection dailyConn = dailyDbSchema.getPoolDataSource().getConnection();
		OracleSchema monthDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(monthDbInfo.getConnectParam()));
		Connection monthConn = monthDbSchema.getPoolDataSource().getConnection();
		LogMover logMover = null;
		try{
			log.info("开始获取日编库履历");
			
			logSelector = new Day2MonPoiLogSelector(dailyDbSchema);
			logSelector.setGrids(gridsOfCity);
			logSelector.setStopTime(syncTimeStamp);
			String tempOpTable = logSelector.select();
			log.info("开始奖日库履历刷新到月库,temptable:"+tempOpTable);
			FlushResult flushResult= new Day2MonLogFlusher(dailyDbSchema,dailyConn,monthConn,true,tempOpTable).flush();
			if(0==flushResult.getTotal()){
				log.info("没有符合条件的履历，不执行日落月，返回");
				return;
			}
			log.info("开始将履历搬到月库：logtotal:"+flushResult.getTotal());
			logMover = new Day2MonMover(dailyDbSchema, monthDbSchema, tempOpTable, flushResult.getTempFailLogTable());
			LogMoveResult logMoveResult = logMover.move();
			log.info("开始进行履历分析");
			
			OperationResult result = parseLog(logMoveResult, monthConn);
			log.info("开始执行前批");
			new PreBatch(result, monthConn).execute();
			log.info("开始执行检查");
			Map<String, Map<Long, Set<String>>> checkResult = new Check(result, monthConn).execute();
			new Classifier(checkResult,monthConn).execute();
			log.info("开始执行后批处理");
			new PostBatch(result,monthConn).execute();
			updateLogCommitStatus(dailyConn,tempOpTable);
			log.info("修改同步信息为成功");
			curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.SUCCESS.getValue());
			d2mSyncApi.updateSyncInfo(curSyncInfo);
			log.info("finished:"+cityId);
		}catch(Exception e){
			if(monthConn!=null)monthConn.rollback();
			if(dailyConn!=null)dailyConn.rollback();
			log.info("rollback db");
			log.error(e.getMessage(),e);
			curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.FAIL.getValue());
			d2mSyncApi.updateSyncInfo(curSyncInfo);
			if(logMover!=null){
				log.info("搬移履历回滚");
				logMover.rollbackMove();
			}
			
		}finally{
			DbUtils.commitAndCloseQuietly(monthConn);
			DbUtils.commitAndCloseQuietly(dailyConn);
			log.info("commit db");
			if(logSelector!=null){
				log.info("释放履历锁");
				logSelector.unselect(false);
			}
			
			
			
		}
		
	}

	protected void updateLogCommitStatus(Connection dailyConn,String tempTable) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql = "update LOG_OPERATION set com_dt = sysdate,com_sta=1,LOCK_STA=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
		run.execute(dailyConn, sql);
		
	}
	private OperationResult parseLog(LogMoveResult logMoveResult, Connection monthConn)
			throws Exception, SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, InstantiationException, OperationResultException {
		Map<Long, List<LogDetail>> logStatInfo = PoiLogDetailStat.loadByOperation(monthConn, logMoveResult.getLogOperationTempTable());
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_ADDRESS");
		OperationResult result = new OperationResult();
		Map<Long,BasicObj> objs =  ObjBatchSelector.selectByPids(monthConn, "IX_POI", tabNames,false, logStatInfo.keySet(), true, true);
		ObjHisLogParser.parse(objs,logStatInfo);
		result.putAll(objs.values());
		return result;
	}

	private FmDay2MonSync createSyncInfo(Day2MonthSyncApi d2mSyncApi, Integer cityId, Date syncTimeStamp) throws Exception {
		FmDay2MonSync info = new FmDay2MonSync();
		info.setCityId(cityId);
		info.setSyncStatus(FmDay2MonSync.SyncStatusEnum.CREATE.getValue());
		info.setJobId(this.getJobInfo().getId());
		Long sid = d2mSyncApi.insertSyncInfo(info );//写入本次的同步信息
		info.setSid(sid);
		info.setSyncTime(syncTimeStamp);
		return info;
	}
	public static void main(String[] args) throws JobException{
		new Day2MonthPoiMergeJob(null).execute();
	}

}
