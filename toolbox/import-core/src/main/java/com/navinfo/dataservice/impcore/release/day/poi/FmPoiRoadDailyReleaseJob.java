package com.navinfo.dataservice.impcore.release.day.poi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.MapHandler;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.Day2MonthSyncApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.Mesh4Partition;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.sys.SysLogConstant;
import com.navinfo.dataservice.bizcommons.sys.SysLogOperator;
import com.navinfo.dataservice.bizcommons.sys.SysLogStats;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.day2mon.Check;
import com.navinfo.dataservice.day2mon.Classifier;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogByFilterGridsSelector;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogSelector;
import com.navinfo.dataservice.day2mon.DeepInfoMarker;
import com.navinfo.dataservice.day2mon.PostBatch;
import com.navinfo.dataservice.day2mon.PreBatch;
import com.navinfo.dataservice.impcore.flushbylog.FlushObjStatInfo;
import com.navinfo.dataservice.impcore.flushbylog.FlushObjStatOperator;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.DailyReleaseLogFlusher;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogFlusher;
import com.navinfo.dataservice.impcore.flusher.DefaultLogFlusher;
import com.navinfo.dataservice.impcore.flusher.LogFlusher;
import com.navinfo.dataservice.impcore.mover.DailyReleaseMover;
import com.navinfo.dataservice.impcore.mover.Day2MonMover;
import com.navinfo.dataservice.impcore.mover.DefaultLogMover;
import com.navinfo.dataservice.impcore.mover.LogMoveResult;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.impcore.selector.DeafultDailyReleaseLogSelector;
import com.navinfo.dataservice.impcore.selector.FmPoiDailyReleaseLogSelector;
import com.navinfo.dataservice.impcore.selector.FmRoadDailyReleaseLogSelector;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import com.navinfo.dataservice.impcore.selector.PoiDailyReleaseLogSelector;
import com.navinfo.dataservice.impcore.statusModifier.DefaultDailyLogStatusModifier;
import com.navinfo.dataservice.impcore.statusModifier.LogStatusModifier;
import com.navinfo.dataservice.impcore.statusModifier.PoiReleaseDailyLogStatusModifier;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;

import net.sf.json.JSONObject;

/** 
 * 日出品刷履历：
       时机：每日定时
       条件：
        1、快线项目关闭范围，Road提该范围全部未出品履历，POI按对象提该范围已完成粗编且未提交出品的履历；
        2、剩余其他范围，POI按对象提该范围已完成粗编且已完成日落月的履历。
     处理：Road履历只刷日出品（P+R）GDB库； POI履历需要刷日产品的2个GDB库。    
 * @ClassName: ReleaseFmIdbDailyPoiJob
 * @author gaopengrong
 * @date 2017年2月17日
 * @Description: FmPoiRoadDailyReleaseJob.java
 */
public class FmPoiRoadDailyReleaseJob extends AbstractJob {
	
	private List<LogSelector> LogSelectors =new ArrayList<LogSelector>();

	private Map<Integer,FlushResult> flushResults;
	
	/**
	 * @param jobInfo
	 */
	public FmPoiRoadDailyReleaseJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	
	/** 
	 * 1、获取可以出品的快线任务信息 
	 * 2、筛选可以出品的log
	 * 3、获取履历写入出品库（两个出品库）
	 * 4、对于单次出品来说，所有项目全部成功，或者全部失败；
	 * @throws  JobException 
	 */
	@Override
	public void execute() throws JobException  {
		boolean isCommon = true;
		boolean commitStatus=false;
		Date syncTimeStamp= new Date();
		
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
		//Day2MonthSyncApi d2mSyncApi = (Day2MonthSyncApi)ApplicationContextUtil.getBean("day2MonthSyncApi");

		try{
			long jobId = this.getJobInfo().getId();
			int oldTaskId = 0;
			int newTaskId = 0;
			Map<String,Map<String,String>> tempTabInfo=null;
			
			FmPoiRoadDailyReleaseJobRequest releaseFmIdbDailyPoiRequest = (FmPoiRoadDailyReleaseJobRequest ) request;
			int releaseFlag=releaseFmIdbDailyPoiRequest.getProduceFlag();//执行标识（0：正常出品，1：补出品）
			
			//确定需要日落月的大区：全部大区
			List<Region> regions = manApi.queryRegionList();
			log.info("确定日落月大区库个数："+regions.size()+"个。");
			response("确定日落月大区库个数："+regions.size()+"个。",null);
			
			//获取可以出品的快线项目信息；
			List<Map<String, Object>> projects =manApi.getProduceProgram();

			if(releaseFlag==1){
				//获取出品记录表中，取日期最新的一次出品记录的taskId、status、tempTab信息；
				Map<String,Object> result=null;
				result= getEarliestFromReleaseTask();
				tempTabInfo=(Map<String,Map<String,String>>) result.get("tempTab");
				int status =(int) result.get("status");
				oldTaskId=(int) result.get("taskId");
				if(status==2){isCommon=false;}
			}
			
			if(isCommon){
				//在man库中，创建一条出品任务记录
				newTaskId=createReleaseTask(String.valueOf(releaseFlag));
				//修改项目的出品状态为 进行中
				updateProduceStatus(projects,1,manApi);
				//修改出品过程表的出品状态为 筛选log中
				updateReleaseTaskStatus(newTaskId,oldTaskId,1,jobId,String.valueOf(projects),null);
				try{
					log.info("开始筛选履历");
					tempTabInfo =logSelect(projects,regions,manApi,databhubApi,syncTimeStamp);
					//修改出品过程表的出品状态为 履历刷库中
					updateReleaseTaskStatus(newTaskId,oldTaskId,2,jobId,null,tempTabInfo);
					log.info("开始刷履历入出品库");	
					logFlush(regions,tempTabInfo,databhubApi);
				}catch(Exception e){
					//修改项目的出品状态为 失败
					updateProduceStatus(projects,3,manApi);
					throw new JobException(e);
				}
			}else{
				//在man库中，创建一条补出品任务记录
				newTaskId=createReleaseTask(String.valueOf(releaseFlag));
				//修改项目的出品状态为 进行中
				updateProduceStatus(projects,1,manApi);
				//修改出品过程表的出品状态为 刷履历中,写入临时表信息
				updateReleaseTaskStatus(newTaskId,oldTaskId,2,jobId,null,tempTabInfo);
				try{
					log.info("开始刷履历入出品库");	
					logFlush(regions,tempTabInfo,databhubApi);
				}catch(Exception e){
					//修改项目的出品状态为 失败
					updateProduceStatus(projects,3,manApi);
					throw new JobException(e);
				}
			}
			//修改项目出品状态为 成功
			updateProduceStatus(projects,2,manApi);
			//修改出品过程表的出品状态为 成功
			updateReleaseTaskStatus(newTaskId,oldTaskId,3,jobId,null,null);
			this.log.info("调用出品转换api");
			callReleaseTransApi();
			//日出品统计
			insertStatInfo();
		}catch(Exception e){
			throw new JobException(e);
		}
		finally{
			//释放日库履历锁
			if(LogSelectors!=null&&LogSelectors.size()>0){
				for(LogSelector ls:LogSelectors){
					unselectLog(ls,commitStatus);
				}
			}
		}
	}
	
	

	private Map<String,Map<String,String>> logSelect(List<Map<String, Object>> projects,List<Region> regions,ManApi manApi,DatahubApi datahubApi,Date syncTimeStamp) throws Exception{
		Map<String,Map<String,String>> tempTabInfo =new HashMap<String,Map<String,String>>();
		LogSelector logSelector =null;
		try{
			//筛选所有大区库的履历
			for(Region region:regions){
				//获取这个region内的所有快线项目的grids(将各项目的grids整合成一个fastPrjGrids)；
				List<Integer> fastPrjGrids = new ArrayList<Integer>();
				for(Map<String, Object> p:projects){
					int produceId = (int) p.get("produceId");
					Map<Integer,Set<Integer>> gridIds = (Map<Integer,Set<Integer>>) p.get("gridIds");
					int dbId =region.getDailyDbId();
					if(gridIds.containsKey(dbId)){
						Set<Integer> grids= gridIds.get(dbId);
						fastPrjGrids.addAll(grids);
					}
				}	
				OracleSchema dbSchema =getSchemaByRegion(region,datahubApi);
				//需重写两个履历筛选的类，一个POI数据的筛选，一个道路的筛选；待实现
				FmPoiDailyReleaseLogSelector poiLogSelector =new FmPoiDailyReleaseLogSelector(dbSchema,fastPrjGrids,syncTimeStamp);
				LogSelectors.add(poiLogSelector);
				String tempTableP = poiLogSelector.select();//先将grids内POI的log按照原则1筛选，写入tempP,再将大区库内所有的poi履历按照原则2筛选履历，merge进tempP、注，截止时间必须一致；
				FmRoadDailyReleaseLogSelector roadLogSelector =new FmRoadDailyReleaseLogSelector(dbSchema,fastPrjGrids,syncTimeStamp);
				LogSelectors.add(roadLogSelector);
				String tempTableR = roadLogSelector.select();//将grids内road的log按照原则1筛选，写入tempP,
				Map<String,String> temps=new HashMap<String,String>();
				temps.put("poi", tempTableP);
				temps.put("road", tempTableR);
				tempTabInfo.put(region.getRegionId().toString(), temps);
			}
			
			return tempTabInfo;
		}catch(Exception e){
			//写执行失败
			throw e;
			
		}finally{
			
		}

	}
	
	private void logFlush(List<Region> regions,Map<String,Map<String,String>> tempTabInfo,DatahubApi datahubApi) throws Exception{
		LogMover logMover = null;
		List<LogMover> logMovers= new ArrayList<LogMover>();
		//大区库链接集合：这个链接是用来写入错误履历的，有异常不需要回滚。别的操作慎用这个链接；
		List<Connection> dailyConns= new ArrayList<Connection>();
		//大区库链接集合：这个链接是用来改出品履历状态的，有异常需要回滚；
		List<Connection> dailyUpdateStatusConns= new ArrayList<Connection>();
		//出品库链接集合：这个链接是用来像出品库刷数据的；
		List<Connection> releaseConns= new ArrayList<Connection>();
		//poi出品库信息
		DbInfo releaseDbP = getReleaseDbConn(datahubApi,"POI");
		OracleSchema releaseDbSchemaP = new OracleSchema(
				DbConnectConfig.createConnectConfig(releaseDbP.getConnectParam()));
		Connection releaseDbConnP = releaseDbSchemaP.getPoolDataSource().getConnection();
		releaseConns.add(releaseDbConnP);
		//poi+road 出品库信息
		//要放开
		DbInfo releaseDbR = getReleaseDbConn(datahubApi,"ROAD");
		OracleSchema releaseDbSchemaR = new OracleSchema(
				DbConnectConfig.createConnectConfig(releaseDbR.getConnectParam()));
		Connection releaseDbConnR = releaseDbSchemaR.getPoolDataSource().getConnection();
		releaseConns.add(releaseDbConnR);
		
		try{
			for(Region region:regions){
			
				Connection dailyConn=getConnectByRegion(region,datahubApi);
				dailyConns.add(dailyConn);
				OracleSchema dailyDbSchema=getSchemaByRegion(region,datahubApi);
				//poi履历临时表
				String tempP=tempTabInfo.get(region.getRegionId().toString()).get("poi");
				//road履历临时表
				String tempR=tempTabInfo.get(region.getRegionId().toString()).get("road");	
				
				//刷poi出品库
				log.info("1.1 开始将日库（"+region.getDailyDbId().toString()+"）poi履历（temptable:"+tempP+"）刷新到P出品库：");
				FlushResult flushResultPP= new DailyReleaseLogFlusher(dailyDbSchema,dailyConn,releaseDbConnP,true,tempP,"").flush();
				if(0!=flushResultPP.getFailedTotal()){
					throw new Exception("存在刷履历失败的log,请查看:"+flushResultPP.getTempFailLogTable());
				}
				log.info("1.2 开始将poi履历搬到P出品库：logtotal:"+String.valueOf(flushResultPP.getTotal()));
				if(0!=flushResultPP.getTotal()){
					logMover = new DailyReleaseMover(dailyDbSchema, releaseDbSchemaP, tempP, flushResultPP.getTempFailLogTable());
					logMovers.add(logMover);
					logMover.move();
				}
				
				
				//刷poi+road出品库
					//刷poi的履历
				log.info("2.1 开始将日库("+region.getDailyDbId().toString()+")poi履历（temptable:"+tempP+"）刷新到P+R出品库");
				FlushResult flushResultPRP= new Day2MonLogFlusher(dailyDbSchema,dailyConn,releaseDbConnR,true,tempP,"fmPoiRoadDailyRelease").flush();
				if(0!=flushResultPRP.getFailedTotal()){
					throw new Exception("存在刷履历失败的log,请查看:"+flushResultPRP.getTempFailLogTable());
				}
				log.info("2.2 开始将poi履历搬到P+R出品库：logtotal:"+String.valueOf(flushResultPRP.getTotal()));
				if(0!=flushResultPRP.getTotal()){
					logMover = new DailyReleaseMover(dailyDbSchema, releaseDbSchemaR, tempP, flushResultPRP.getTempFailLogTable());
					logMovers.add(logMover);
					logMover.move();
				}
					//刷road的履历
				log.info("3.1 开始将日库("+region.getDailyDbId().toString()+")road履历（temptable:"+tempR+"）刷新到P+R出品库");
				FlushResult flushResultPRR= new Day2MonLogFlusher(dailyDbSchema,dailyConn,releaseDbConnR,true,tempR,"fmPoiRoadDailyRelease").flush();
				if(0!=flushResultPRR.getFailedTotal()){
					throw new Exception("存在刷履历失败的log,请查看:"+flushResultPRR.getTempFailLogTable());
				}
				log.info("3.2 开始将road履历搬到P+R出品库：logtotal:"+String.valueOf(flushResultPRR.getTotal()));
				if(0!=flushResultPRR.getTotal()){
					logMover = new DailyReleaseMover(dailyDbSchema, releaseDbSchemaR, tempR, flushResultPRR.getTempFailLogTable());
					logMovers.add(logMover);
					logMover.move();
				}
				
				Connection dailyUpdateStatusConn = dailyDbSchema.getPoolDataSource().getConnection();
				dailyUpdateStatusConns.add(dailyUpdateStatusConn);
				//更新状态	
				LogStatusModifier logStatusModifierPP = createLogStatusModifier("ROAD",dailyDbSchema,tempP);
				logStatusModifierPP.execute(dailyUpdateStatusConn);
				log.info("完成P+R库出品状态更新（poi）");
				
				//更新poi状态
				LogStatusModifier logStatusModifierP = createLogStatusModifier("POI",dailyDbSchema,tempP);
				logStatusModifierP.execute(dailyUpdateStatusConn);
				log.info("完成P库出品状态更新（poi）");
				
				//更新状态	
				LogStatusModifier logStatusModifierPR = createLogStatusModifier("ROAD",dailyDbSchema,tempR);
				logStatusModifierPR.execute(dailyUpdateStatusConn);
				log.info("完成P+R库出品状态更新（road）");
				
				//进行履历对象级统计
				try {
					//处理poi刷库记录
					String tempFailLogTableP = flushResultPRP.getTempFailLogTable();
					//统计总数
					Map<String, Integer> totalObjStatP = FlushObjStatOperator.getTotalObjStatByLog(dailyConn, tempP);
					//统计失败的数量
					Map<String, Integer> failObjStatP = FlushObjStatOperator.getTotalObjStatByLog(dailyConn, tempFailLogTableP);
					//处理统计数据
					if(totalObjStatP != null && totalObjStatP.size() > 0){
						for(Entry<String, Integer> entry : totalObjStatP.entrySet()){
							String objName = entry.getKey();
							int total = entry.getValue();
							int failTotal = 0;
							if(failObjStatP != null && failObjStatP.size() > 0){
								if(failObjStatP.containsKey(objName)){
									failTotal = failObjStatP.get(objName);
								}
							}
							//保存数据
							FlushObjStatInfo objStatInfo = new FlushObjStatInfo(objName);
							objStatInfo.setTotal(total);
							objStatInfo.setSuccess(total-failTotal);
							
							flushResultPRP.addObjStatInfo(objStatInfo);
						}
					}
					//处理道路刷库记录
					String tempFailLogTableR = flushResultPRR.getTempFailLogTable();
					//统计总数
					Map<String, Integer> totalObjStatR = FlushObjStatOperator.getTotalObjStatByLog(dailyConn, tempR);
					//统计失败的数量
					Map<String, Integer> failObjStatR = FlushObjStatOperator.getTotalObjStatByLog(dailyConn, tempFailLogTableR);
					//处理统计数据
					if(totalObjStatR != null && totalObjStatR.size() > 0){
						for(Entry<String, Integer> entry : totalObjStatR.entrySet()){
							String objName = entry.getKey();
							int total = entry.getValue();
							int failTotal = 0;
							if(failObjStatR != null && failObjStatR.size() > 0){
								if(failObjStatR.containsKey(objName)){
									failTotal = failObjStatR.get(objName);
								}
							}
							//保存数据
							FlushObjStatInfo objStatInfo = new FlushObjStatInfo(objName);
							objStatInfo.setTotal(total);
							objStatInfo.setSuccess(total-failTotal);
							
							flushResultPRR.addObjStatInfo(objStatInfo);
						}
					}
					//将poi和道路的结果合并
					int total = flushResultPRP.getTotal() + flushResultPRR.getTotal();
					int insertTotal = flushResultPRP.getInsertTotal() + flushResultPRR.getInsertTotal();
					int updateTotal = flushResultPRP.getUpdateTotal() + flushResultPRR.getUpdateTotal();
					int deleteTotal = flushResultPRP.getDeleteTotal() + flushResultPRR.getDeleteTotal();
					int failedTotal = flushResultPRP.getFailedTotal() + flushResultPRR.getFailedTotal();
					int insertFailed = flushResultPRP.getInsertFailed() + flushResultPRR.getInsertFailed();
					int updateFailed = flushResultPRP.getUpdateFailed() + flushResultPRR.getUpdateFailed();
					int deleteFailed = flushResultPRP.getDeleteFailed() + flushResultPRR.getDeleteFailed();
					String tempFailLogTable = "poi:"+flushResultPRP.getTempFailLogTable()+",road:"+flushResultPRR.getTempFailLogTable();
					Set<FlushObjStatInfo> objStatInfo = new HashSet<FlushObjStatInfo>();
					objStatInfo.addAll(flushResultPRP.getObjStatInfo());
					objStatInfo.addAll(flushResultPRR.getObjStatInfo());
					
					FlushResult flushResult = new FlushResult();
					flushResult.setTotal(total);
					flushResult.setInsertTotal(insertTotal);
					flushResult.setUpdateTotal(updateTotal);
					flushResult.setDeleteTotal(deleteTotal);
					
					flushResult.setFailedTotal(failedTotal);
					flushResult.setInsertFailed(insertFailed);
					flushResult.setUpdateFailed(updateFailed);
					flushResult.setDeleteFailed(deleteFailed);
					
					flushResult.setTempFailLogTable(tempFailLogTable);
					flushResult.addObjStatInfos(objStatInfo);
					
					if(flushResults == null){
						flushResults = new HashMap<Integer,FlushResult>();
					}
					flushResults.put(region.getRegionId(), flushResult);
				} catch (Exception e) {
					log.error("大区库(regionId:"+region.getRegionId()+")按对象统计日出品失败,"+e.getMessage());
				}
				
			}
			
		}catch(Exception e){
			if(releaseDbConnP!=null)releaseDbConnP.rollback();
			if(releaseDbConnR!=null)releaseDbConnR.rollback();
			for(Connection d2conn:dailyUpdateStatusConns){
				DbUtils.rollbackAndCloseQuietly(d2conn);
			}
			log.info("rollback db");
			if(logMovers!=null){
				for(LogMover l:logMovers){
					log.info("搬移履历回滚");
					l.rollbackMove();
				}
			}
			throw e;
			
		}finally{
			DbUtils.commitAndCloseQuietly(releaseDbConnP);
			DbUtils.commitAndCloseQuietly(releaseDbConnR);
			//这个链接是用来写入错误履历的，因此不需要回滚。别的操作慎用这个链接；
			for(Connection dconn:dailyConns){
				DbUtils.commitAndCloseQuietly(dconn);
			}
			for(Connection d2conn:dailyUpdateStatusConns){
				DbUtils.commitAndCloseQuietly(d2conn);
			}
			log.info("commit db");
			
		}
		
	}
	
	private LogStatusModifier createLogStatusModifier(String featureType,
			OracleSchema srcDbSchema, String tempTable) {
		if ("POI".equals(featureType)){
			return new PoiReleaseDailyLogStatusModifier(srcDbSchema,tempTable);
		}
		return new DefaultDailyLogStatusModifier(srcDbSchema,tempTable);
	}
	
	private Connection getConnectByRegion(Region region,DatahubApi datahubApi) throws Exception{
		Integer dbId = region.getDailyDbId();
		DbInfo dbInfo = datahubApi.getDbById(dbId);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		Connection conn = dbSchema.getPoolDataSource().getConnection();
		return conn;
	}
	private OracleSchema getSchemaByRegion(Region region,DatahubApi datahubApi) throws Exception{
		Integer dbId = region.getDailyDbId();
		DbInfo dbInfo = datahubApi.getDbById(dbId);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		return dbSchema;
	}
	
	private DbInfo getReleaseDbConn(DatahubApi databhubApi,String featureType) throws Exception {
		if ("POI".equals(featureType)){
			return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_POI.getValue());
		}
		return databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.DES_DAY_ALL.getValue());
	}
	
	private int createReleaseTask(String produceType) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			int taskId=getNewTaskId(conn);
			QueryRunner run = new QueryRunner();
			String createSql = "insert into RELEASE_TASK (TASK_ID,RELEASE_TYPE,create_date,RELEASE_STATUS) "
					+ "values("+taskId+",'"+produceType+"',sysdate,0)";			
			log.debug("创建出品任务sql:"+createSql);
			run.update(conn,createSql);		
			return taskId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private static int getNewTaskId(Connection conn) throws Exception {
		// TODO Auto-generated method stub
		try{
			QueryRunner run = new QueryRunner();
			String querySql = "select Release_SEQ.NEXTVAL as TASK_ID from dual";
			int taskId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("TASK_ID")
					.toString());
			return taskId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}
	}
	private Map<String,Object> getEarliestFromReleaseTask() throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			Map<String,Object> result= new HashMap<String,Object>();
			conn = DBConnector.getInstance().getManConnection();
			String sql = "SELECT TASK_ID,RELEASE_STATUS,TEMP_TAB FROM (select * from RELEASE_TASK ORDER BY CREATE_DATE DESC) WHERE ROWNUM = 1 ";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()){
				int task_id=rs.getInt("TASK_ID");
				String str=rs.getString("TEMP_TAB");
				JSONObject  jasonObject = JSONObject.fromObject(str);
				int status=rs.getInt("RELEASE_STATUS");
				result.put("taskId", task_id);
				result.put("tempTab", jasonObject);
				result.put("status", status);
			}
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
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
	
	private static void updateReleaseTaskStatus(int taskId,int oldTaskId,int status,long jobId,String jsonProject,Map<String,Map<String,String>> jsonTempTab) throws Exception {
		Connection conn = null;
		try{
			JSONObject jsonObject = JSONObject.fromObject(jsonTempTab); 
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("update RELEASE_TASK set RELEASE_STATUS="+status);
			if(oldTaskId!=0){sb.append(",REF_TASK_ID="+oldTaskId);}
			if(jobId!=0){sb.append(",JOBID='"+String.valueOf(jobId)+"'");}
			if(jsonProject!=null){sb.append(",PARAMETER='"+jsonProject+"'");}
			if(jsonTempTab!=null){sb.append(",TEMP_TAB='"+jsonObject.toString()+"'");}
			sb.append(" where task_Id="+taskId);
			String sql = sb.toString();
			run.update(conn,sql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private void updateProduceStatus(List<Map<String, Object>> projects,int status, ManApi manApi) throws Exception{
		for(Map<String, Object> p:projects){
			int produceId=(int) p.get("produceId");
			manApi.updateProduceStatus(produceId, status);
		}
	}
	
	private void callReleaseTransApi() throws IOException {
		try { 
			ServiceInvokeUtil http =new ServiceInvokeUtil();
			String msUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.productConvert);
			String json=http.invoke(msUrl,null,10);
			log.info("调用出品转换接口:"+json);
		 } catch (Exception e) {
			 log.debug("调用出品转换接口报错:"+e.getMessage());
	     } 
	}
	
	
	/**
	 * 将统计信息存入sys库中FM_LOG_STATS
	 * @author Han Shaoming
	 * @param beginTime
	 */
	private void insertStatInfo()  {
		try{
			//设置导入成功状态
			long jobId = jobInfo.getId();
			String beginTime = DateUtils.dateToString(jobInfo.getBeginTime(), "yyyy/MM/dd HH:mm:ss");
			//执行插入
			if(flushResults != null && flushResults.size() > 0){
				for (Entry<Integer, FlushResult> entry : flushResults.entrySet()) {
					int regionId = entry.getKey();
					FlushResult flushResult = entry.getValue();
					try {
						int successTotal = flushResult.getTotal() - flushResult.getFailedTotal();
						int failureTotal = flushResult.getFailedTotal();
						int total = flushResult.getTotal();
						//处理日志分类描述
						StringBuilder logDesc = new StringBuilder();
						logDesc.append(" ,jobId:"+jobId+" ,regionId:"+regionId);
						Set<FlushObjStatInfo> objStatInfos = flushResult.getObjStatInfo();
						if(objStatInfos != null && objStatInfos.size() > 0){
							for (FlushObjStatInfo objStatInfo : objStatInfos) {
								String objName = objStatInfo.getObjName();
								//同一关系的过滤
								if("IX_SAMEPOI".equals(objName)){continue;}
								int totalObj = objStatInfo.getTotal();
								int successObj = objStatInfo.getSuccess();
								logDesc.append(" ,"+objName+":"+successObj+"/"+totalObj);
							}
						}
						//处理失败描述
						String errorMsg = null;
						if(failureTotal > 0){
							errorMsg = "存在刷履历失败的log,请查看:"+flushResult.getTempFailLogTable();
						}
								
						SysLogStats log = new SysLogStats();
						log.setLogType(SysLogConstant.DAY_TO_PRODUCT);
						log.setLogDesc(SysLogConstant.DAY_TO_PRODUCT_DESC+logDesc.toString());
						log.setFailureTotal(failureTotal);
						log.setSuccessTotal(successTotal);  
						log.setTotal(total);
						log.setBeginTime(beginTime);
						log.setEndTime(DateUtils.getSysDateFormat());
						log.setErrorMsg(errorMsg);
						log.setUserId("0");
						
						SysLogOperator.getInstance().insertSysLog(log);
						
					} catch (Exception e) {
						log.error("大区库(regionId:"+regionId+")日出品统计入库出错,"+e.getMessage());
					}
				}
			}
		}catch (Exception e) {
			log.error("日出品统计入库出错："+e.getMessage(), e);
		}
	}
	
}
