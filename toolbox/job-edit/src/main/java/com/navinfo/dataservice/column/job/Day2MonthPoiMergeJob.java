package com.navinfo.dataservice.column.job;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;

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
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationResultException;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.day2mon.Check;
import com.navinfo.dataservice.day2mon.Classifier;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogByFilterGridsSelector;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogByTaskIdSelector;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogSelector;
import com.navinfo.dataservice.day2mon.DeepInfoMarker;
import com.navinfo.dataservice.day2mon.PoiGuideLinkBatch;
import com.navinfo.dataservice.day2mon.PostBatch;
import com.navinfo.dataservice.day2mon.PreBatch;
import com.navinfo.dataservice.impcore.exception.LockException;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogFlusher;
import com.navinfo.dataservice.impcore.mover.Day2MonMover;
import com.navinfo.dataservice.impcore.mover.LogMoveResult;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

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
	private List<LogMover> logMovers= new ArrayList<LogMover>();

	public Day2MonthPoiMergeJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException{
		ManApi manApi = (ManApi)ApplicationContextUtil
				.getBean("manApi");
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil
				.getBean("datahubApi");
		Day2MonthSyncApi d2mSyncApi = (Day2MonthSyncApi)ApplicationContextUtil
				.getBean("day2MonthSyncApi");
		MetadataApi metaApi = (MetadataApi)ApplicationContextUtil
				.getBean("metadataApi");
		long phaseId =0;
		JSONObject logInfo =new JSONObject();
		try {
			Day2MonthPoiMergeJobRequest day2MonRequest=(Day2MonthPoiMergeJobRequest) request;
			int type = day2MonRequest.getType();//快线还是中线:0 中线，1 快线
			int lot = day2MonRequest.getLot();//中线批次:0,1,2,3(快线输0)
			phaseId =(long) day2MonRequest.getPhaseId();
			List<Integer> specRegionId=day2MonRequest.getSpecRegionId();
			Map<Integer,List<Integer>> subTasks = (Map<Integer,List<Integer>>)day2MonRequest.getTaskInfo();
			
			DbInfo dbInfo = datahubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS.getValue());

			//确定需要日落月的大区
			List<Region> regions = new ArrayList<Region>();;
			if(subTasks!=null&&subTasks.size()>0){//判断是否按任务落
				for(Object obj:subTasks.keySet()) {
					int regionId=0;
					if(obj instanceof Integer){  
		                regionId=(int) obj;
					}else if(obj instanceof String){  
						regionId =Integer.parseInt(String.valueOf(obj));
		            }
					Region r = manApi.queryByRegionId(regionId);
					regions.add(r);
				} 
			}else if(specRegionId!=null||specRegionId.size()>0){//按照指定大区库进行日落月
				for(int regionId:specRegionId){
					Region r = manApi.queryByRegionId(regionId);
					regions.add(r);
				}
			}else{
				//全部大区定时落
				regions = manApi.queryRegionList();
			}
			log.info("确定日落月大区库个数："+regions.size()+"个。");
			response("确定日落月大区库个数："+regions.size()+"个。",null);
			
			if(type==1){//快线任务日落月，所有日大区库统一成功、统一失败回滚
				try {
					
					/** 一、快线任务
					1、根据输入的 大区及大区内的快线任务，分别去每个大区库中筛选相应的快线任务涉及的数据履历。
					2、筛选到履历后，获取所有的grids;
					3、将所有的grids转换成对应的meshes；
					4、拿所有的meshes申请DMS锁；
					5、若存在申请不到DMS锁的图幅，则报出具体图幅被锁信息，且所有大区库均日落月失败；
					6、若所有图幅均申请成功DMS锁，则所有大区库均日落月成功；
					备注：所有大区库刷月库保持事务一致性；
					*/
					
					List<Integer> allLogGrids = new ArrayList<Integer>();
					for(Region region:regions){
						
						//1、获取该大区库下面的任务号
						List<Integer> taskIds = subTasks.get(region.getRegionId().toString());
						//2、获取履历所在所有grids
						List<Integer> logGrids =selectLogGridsByTaskId(taskIds,datahubApi,region,type);
						allLogGrids.addAll(logGrids);
					}
					//3、将所有的grids转换成对应的meshes；
					List<Integer> allLogMeshes = grids2meshs(allLogGrids);
					//4、拿所有的meshes申请DMS锁；
					Map<Integer,String>  dmsLockMeshes= new HashMap<Integer,String>();
					log.info("以下图幅申请DMS锁:"+allLogMeshes.toString());
					if(allLogMeshes!=null&&allLogMeshes.size()>0){
						dmsLockMeshes = getDmsLock(allLogMeshes,jobInfo.getId(),dbInfo);
						
						Connection monthConn=getGdbConnect(datahubApi);
						OracleSchema monthDbSchema=getGdbSchema(datahubApi);
						
						//5、若存在申请不到DMS锁的图幅，则报出具体图幅被锁信息，且所有大区库均日落月失败；
						if(dmsLockMeshes!=null&&dmsLockMeshes.size()>0){
							logInfo.put("dmsLockMeshes", dmsLockMeshes);
							manApi.updateJobProgress(phaseId,3,logInfo.toString());
							log.info("以下图幅存在需要落的数据，但是对应的图幅没有申请到DMS锁，请处理后再落:"+dmsLockMeshes.toString());
							throw new Exception("以下gird存在需要落的数据，但是对应的图幅没有申请到DMS锁，请处理后再落:"+dmsLockMeshes.toString());
						}else{
							//6、若所有图幅均申请成功DMS锁，则所有日大区库均日落月成功（有异常则所有的都失败）；
							
							//大区库链接集合：这个链接用来挪履历，有异常需要回滚；
							List<Connection> dailyConns= new ArrayList<Connection>();
							//大区库链接集合：这个链接是用来改出品履历状态的，有异常需要回滚；
							List<LogSelector> LogSelectors= new ArrayList<LogSelector>();
							
							OperationResult allResult=new OperationResult();
							String tempPoiGLinkTab ="";
							boolean isbatch = true;
							try{
								for(Region region:regions){
									
									log.info("获取大区库连接信息:"+region);
									if(region==null) return ;							
									
									OperationResult result=new OperationResult();
									Date syncTimeStamp= new Date();
									
									Connection dailyConn=getConnectByRegion(region,datahubApi,"daily");
									dailyConns.add(dailyConn);
									OracleSchema dailyDbSchema=getSchemaByRegion(region,datahubApi,"daily");
			
									List<Integer> taskIds = subTasks.get(region.getRegionId().toString());
									
									log.info("开始获取日编库相关快线任务履历"+taskIds.toString());
									LogSelector logSelector = new Day2MonPoiLogByTaskIdSelector(dailyDbSchema,syncTimeStamp,null,taskIds,1);
									LogSelectors.add(logSelector);
									String tempOpTable = logSelector.select();

									log.info("开始将日库履历刷新到月库,temptable:"+tempOpTable);
									result=logFlushAndBatchData( monthDbSchema, monthConn, dailyDbSchema,dailyConn,tempOpTable);
									
									allResult.putAll(result.getAllObjs());
									log.info("大区库（regionId:"+region.getRegionId()+"）日落月刷库完成。");
								}
								
//								log.info("开始筛选需要批引导LINK的POI");
//								tempPoiGLinkTab=createPoiTabForBatchGL(allResult,monthDbSchema);
//								log.info("需要执行引导LINK批处理的POI在临时表中："+tempPoiGLinkTab);
								logInfo.put("allQuickMeshes", allLogMeshes);
							
							}catch(Exception e){
								logInfo.put("errmsg", e.getMessage());
								isbatch = false;
								if(monthConn!=null)monthConn.rollback();
								for(Connection dconn:dailyConns){
									dconn.rollback();
								}
								if(logMovers!=null){
									for(LogMover l:logMovers){
										log.info("搬移履历回滚");
										l.rollbackMove();
									}
								}
								log.info("rollback db");
								throw e;
								
							}finally{
								DbUtils.commitAndCloseQuietly(monthConn);
								for(Connection dconn:dailyConns){
									DbUtils.commitAndCloseQuietly(dconn);
								}
								log.info("commit db");
								for(LogSelector LogSelector:LogSelectors){
									log.info("释放履历锁");
									LogSelector.unselect(false);
								}
//								if(isbatch&&!tempPoiGLinkTab.isEmpty()){
//									log.info("开始执行引导LINK批处理");
//									new PoiGuideLinkBatch(tempPoiGLinkTab,monthDbSchema).execute();
//									log.info("引导LINK批处理执行完成");
//								}
							}
						}
					}
					
				}catch(Exception e){
					callDmsReleaseLockApi(jobInfo.getId());
					manApi.updateJobProgress(phaseId,3,logInfo.toString());
					log.error(e.getMessage(), e);
					throw new JobException(e.getMessage(),e);
				}
			}else{//中线任务日落月，单个日大区库成功，单个儿日大区库失败
				try {
					List<Integer> logCloseLot = new ArrayList<Integer>();//中线按任务落：当前批次关闭的图幅
					List<Integer> logCloseUnLot = new ArrayList<Integer>();//中线按任务落：非当前批次关闭的图幅
					Map<Integer,String>  logDmsLockLot= new HashMap<Integer,String>();//中线按任务落：当前批次未申请到DMS锁的图幅
					Map<Integer,String>  logDmsLockUnLot= new HashMap<Integer,String>();//中线按任务落：非当前批次未申请到DMS锁的图幅
					
					for(Region region:regions){
						
						if(subTasks==null||subTasks.size()==0){
							/**中线任务
							1）每天定时落
							①获取所有的大区库，依次日落月每个大区库
							②每个大区库中查询：粗编完成且图幅开关为开启的数据履历信息；
							③筛选到履历后，获取所有的grids;
							④将所有的grids转换成对应的meshes；
							⑤拿所有的meshes申请DMS锁；
							⑥若存在申请不到DMS锁的图幅，则报出具体图幅被锁信息，申请到锁的图幅执行日落月*/
							
							List<Integer> filterGrids = new ArrayList<Integer>();
							List<Integer> logGrids =selectLogGridsByTaskId(null,datahubApi,region,type);//查询所有存在可落履历的grids
							List<Integer> logMeshes = grids2meshs(logGrids);
							List<Integer> closeMeshes = metaApi.getMeshsFromPartition(logMeshes,0,0);//查询关闭的图幅
							
							filterGrids.addAll(meshs2grids(closeMeshes));
							log.info("以下关闭图幅内的数据履历未日落月："+closeMeshes.toString());
							
							logMeshes.removeAll(closeMeshes);//拿所有的未关闭的meshes申请DMS锁；
							Map<Integer,String> dmsLockInfo =new HashMap<Integer,String>();
							dmsLockInfo = getDmsLock(logMeshes,jobInfo.getId(),dbInfo);
							
							List<Integer> dmsLockMeshes = new ArrayList<Integer>();
							dmsLockMeshes.addAll(dmsLockInfo.keySet());
							
							filterGrids.addAll(meshs2grids(dmsLockMeshes));
							log.info("以下未申请到DMS锁的图幅，未日落月："+dmsLockMeshes.toString());

							doMediumSync(region,filterGrids,null,null,datahubApi,d2mSyncApi,manApi);

						}else{
							/**2）按批次补落：
							①根据输入的 大区及大区内的中线任务，分别去每个大区库中筛选相应的中线任务涉及的数据履历。
							②筛选到履历后，获取所有的grids;
							③将所有的grids转换成对应的meshes；
							④筛选图幅开关为“开启”的图幅（未开启需记录下来，返回给管理平台）
							⑤拿所有开启的meshes申请DMS锁；（未申请到锁的记录下来，返回给管理平台）
							⑥日落月申请到DMS锁的图幅对应的中线任务的履历；*/
							
							List<Integer> taskIds = subTasks.get(region.getRegionId().toString());
							List<Integer> logGrids =selectLogGridsByTaskId(taskIds,datahubApi,region,type);//按任务查询所有存在可落履历的grids
							List<Integer> logMeshes = grids2meshs(logGrids);
							List<Integer> closeMeshes = metaApi.getMeshsFromPartition(logMeshes,0,0);//查询关闭的图幅
							
							List<Integer> lotCloseMeshes = metaApi.getMeshsFromPartition(logMeshes,0,lot);//查询当前批次关闭的图幅
							logCloseLot.addAll(lotCloseMeshes);//返给管理平台：当前批次关闭图幅
							
							logMeshes.removeAll(closeMeshes);//删除关闭图幅，申请DMS锁；
							
							List<Integer> openLot = metaApi.getMeshsFromPartition(logMeshes,1,lot);//用当前批次开启的图幅，申请DMS锁
							Map<Integer,String>  openLotDmsLockInfo= new HashMap<Integer,String>();
							openLotDmsLockInfo = getDmsLock(openLot,jobInfo.getId(),dbInfo);
							
							
							List<Integer> openUnLot = new ArrayList<Integer>();
							openUnLot.addAll(logMeshes);
							openUnLot.removeAll(openLot);//用非当前批次开启的图幅，申请DMS锁
							Map<Integer,String>  openUnLotDmsLockInfo= new HashMap<Integer,String>();
							openUnLotDmsLockInfo = getDmsLock(openUnLot,jobInfo.getId(),dbInfo);
							
							closeMeshes.removeAll(lotCloseMeshes);//获取非当前批次关闭图幅
							logCloseUnLot.addAll(closeMeshes);//返给管理平台：当前批次关闭图幅
							logDmsLockLot.putAll(openLotDmsLockInfo);//返给管理平台：当前批次未申请到DMS锁的图幅
							logDmsLockUnLot.putAll(openUnLotDmsLockInfo);//返给管理平台：非当前批次未申请到DMS锁的图幅
							
							List<Integer> lotDmsLockInfos = new ArrayList<Integer>();
							for(Object set:openLotDmsLockInfo.keySet()){
								lotDmsLockInfos.add(Integer.parseInt(set.toString()));
							}
							List<Integer> unLotDmsLockInfos = new ArrayList<Integer>();
							for(Object set:openUnLotDmsLockInfo.keySet()){
								unLotDmsLockInfos.add(Integer.parseInt(set.toString()));
							}

							logMeshes.removeAll(lotDmsLockInfos);//删除未申请到DMS图幅锁的图幅
							logMeshes.removeAll(unLotDmsLockInfos);//删除未申请到DMS图幅锁的图幅
							
							List<Integer> grids = meshs2grids(logMeshes);
							if(grids!=null&&grids.size()>0){
								doMediumSync(region,null,grids,taskIds,datahubApi,d2mSyncApi,manApi);
							}else{
								log.info("没有满足条件的可落图幅，不执行日落月，返回");
							}
						}
					}
					logInfo.put("closeLot", logCloseLot);
					logInfo.put("closeUnLot", logCloseUnLot);
					logInfo.put("dmsLockLot", logDmsLockLot);
					logInfo.put("dmsLockUnLot", logDmsLockUnLot);
					log.info("图幅状态："+logInfo.toString());
					
				}catch(Exception e){
					log.info("图幅状态："+logInfo.toString());
					callDmsReleaseLockApi(jobInfo.getId());
					logInfo.put("errmsg", e.getMessage());
					if(phaseId!=0){
						manApi.updateJobProgress(phaseId,3,logInfo.toString());
					}
					log.error(e.getMessage(), e);
					throw new JobException(e.getMessage(),e);
				}
			}
			callDmsReleaseLockApi(jobInfo.getId());
			if(phaseId!=0){
				manApi.updateJobProgress(phaseId,2,logInfo.toString());
			}
		}catch(Exception e){
			logInfo.put("errmsg", e.getMessage());
			if(phaseId!=0){
				try {					
					manApi.updateJobProgress(phaseId,3,logInfo.toString());
				} catch (Exception e1) {
					throw new JobException(e1.getMessage(),e1);
				}
			}
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}

	}
	
	private Connection getConnectByRegion(Region region,DatahubApi datahubApi,String flag) throws Exception{
		log.info("获取大区库连接信息:"+region);
		Integer dbId = 0;
		if(flag.equals("daily")){
			dbId = region.getDailyDbId();
			log.info("获取日大区库dbId:"+dbId);
		}else{
			dbId = region.getMonthlyDbId();
			log.info("获取月大区库dbId:"+dbId);
		}
		DbInfo dbInfo = datahubApi.getDbById(dbId);
		log.info("获取数据库信息:"+dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		Connection conn = dbSchema.getPoolDataSource().getConnection();
		return conn;
	}
	private OracleSchema getSchemaByRegion(Region region,DatahubApi datahubApi,String flag) throws Exception{
		log.info("获取大区库连接信息:"+region);
		Integer dbId = 0;
		if(flag.equals("daily")){
			dbId = region.getDailyDbId();
			log.info("获取日大区库dbId:"+dbId);
		}else{
			dbId = region.getMonthlyDbId();
			log.info("获取月大区库dbId:"+dbId);
		}
		DbInfo dbInfo = datahubApi.getDbById(dbId);
		log.info("获取数据库信息:"+dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		return dbSchema;
	}
	private OracleSchema getGdbSchema(DatahubApi databhubApi) throws Exception{
		DbInfo dbInfo = databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS.getValue());
		log.info("获GDB母库连接信息:"+dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		return dbSchema;
	}
	private Connection getGdbConnect(DatahubApi databhubApi) throws Exception{
		DbInfo dbInfo = databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS.getValue());
		log.info("获GDB母库连接信息:"+dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		Connection conn = dbSchema.getPoolDataSource().getConnection();
		return conn;
	}
	

	private void doMediumSync(Region region,List<Integer> filterGrids,List<Integer> grids,List<Integer> taskIds,DatahubApi datahubApi, Day2MonthSyncApi d2mSyncApi,ManApi manApi)
			throws Exception{
		
		//1. 获取最新的成功同步信息，并记录本次同步信息
		FmDay2MonSync lastSyncInfo = d2mSyncApi.queryLastedSyncInfo(region.getRegionId());
		log.info("获取最新的成功同步信息："+lastSyncInfo);		
		Date syncTimeStamp= new Date();
		FmDay2MonSync curSyncInfo = createSyncInfo(d2mSyncApi, region.getRegionId(),syncTimeStamp);//记录本次的同步信息
		d2mSyncApi.insertSyncInfo(curSyncInfo);
		
		log.info("获取大区库连接信息:"+region);
		if(region==null) return ;

		Connection dailyConn=getConnectByRegion(region,datahubApi,"daily");
		OracleSchema dailyDbSchema=getSchemaByRegion(region,datahubApi,"daily");
		Connection monthConn=getConnectByRegion(region,datahubApi,"month");
		OracleSchema monthDbSchema=getSchemaByRegion(region,datahubApi,"month");
		
		LogSelector logSelector = null;
		
		String tempPoiGLinkTab ="";
		boolean isbatch = true;
		OperationResult result=new OperationResult();
		try{
			
			log.info("开始获取日编库履历");
			String tempOpTable="";
			if(grids!=null&&grids.size()>0){
				logSelector = new Day2MonPoiLogByTaskIdSelector(dailyDbSchema,syncTimeStamp,grids,taskIds,0);
				tempOpTable = logSelector.select();
			}else{
				logSelector = new Day2MonPoiLogByFilterGridsSelector(dailyDbSchema,syncTimeStamp,filterGrids,0);
				tempOpTable = logSelector.select();
			}
			
			result=logFlushAndBatchData( monthDbSchema, monthConn, dailyDbSchema,dailyConn,tempOpTable);
			log.info("修改同步信息为成功");
			curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.SUCCESS.getValue());
			d2mSyncApi.updateSyncInfo(curSyncInfo);
			log.info("finished:"+region.getRegionId());
			
//			log.info("开始筛选需要批引导LINK的POI");
//			tempPoiGLinkTab=createPoiTabForBatchGL(result,monthDbSchema);
//			log.info("需要执行引导LINK批处理的POI在临时表中："+tempPoiGLinkTab);
			
		}catch(Exception e){
			isbatch = false;
			if(monthConn!=null)monthConn.rollback();
			if(dailyConn!=null)dailyConn.rollback();
			log.info("rollback db");
			try{
				curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.FAIL.getValue());
				d2mSyncApi.updateSyncInfo(curSyncInfo);
			}catch(Exception ee){
				log.info("回滚任务状态报错："+ee.getMessage());
			}
			
			if(logMovers!=null){
				for(LogMover l:logMovers){
					log.info("搬移履历回滚");
					l.rollbackMove();
				}
			}
			throw e;
			
		}finally{
			DbUtils.commitAndCloseQuietly(monthConn);
			DbUtils.commitAndCloseQuietly(dailyConn);
			log.info("commit db");
			if(logSelector!=null){
				log.info("释放履历锁");
				logSelector.unselect(false);
			}
//			if(isbatch&&!tempPoiGLinkTab.isEmpty()){
//				log.info("开始执行引导LINK批处理");
//				new PoiGuideLinkBatch(tempPoiGLinkTab,monthDbSchema).execute();
//				log.info("引导LINK批处理执行完成");
//			}
			
		}
		
	}
	
	private OperationResult logFlushAndBatchData(OracleSchema monthDbSchema,Connection monthConn,OracleSchema dailyDbSchema,Connection dailyConn,String tempOpTable)
			throws Exception{
		
		OperationResult result=new OperationResult();
		try{
			
			FlushResult flushResult= new Day2MonLogFlusher(dailyDbSchema,dailyConn,monthConn,true,tempOpTable,"day2MonSync").flush();
			if(0==flushResult.getTotal()){
				log.info("没有符合条件的履历，不执行日落月，返回");
			}else{
				log.info("开始将履历搬到月库：logtotal:"+flushResult.getTotal());
				//快线搬移履历是传进去的日大区库连接（刷库用的连接），如果出现异常，回滚日大区库连接即可；
				LogMover logMover = new Day2MonMover(dailyDbSchema, monthDbSchema, tempOpTable, flushResult.getTempFailLogTable());
				logMovers.add(logMover);
				LogMoveResult logMoveResult = logMover.move();
				log.info("开始进行履历分析");
				result = parseLog(logMoveResult, monthConn);
				if(result.getAllObjs().size()>0){
					log.info("开始进行深度信息打标记");
					new DeepInfoMarker(result,monthConn).execute();
					log.info("开始执行前批");
					new PreBatch(result, monthConn).execute();
					log.info("开始执行检查");
					Map<String, Map<Long, Set<String>>> checkResult = new Check(result, monthConn).execute();
					new Classifier(checkResult,monthConn).execute();
					log.info("开始执行后批处理");
					new PostBatch(result,monthConn).execute();
					log.info("开始批处理MESH_ID_5K、ROAD_FLAG、PMESH_ID");
					updateField(result, monthConn);
				}
				updateLogCommitStatus(dailyConn,tempOpTable);
			}

			return result;
		}catch(Exception e){
			throw e;
		}finally{

		}
		
	}

	

	private String createPoiTabForBatchGL(OperationResult opResult, OracleSchema monthDbSchema) throws Exception{
		Connection conn = monthDbSchema.getPoolDataSource().getConnection();
		int count=0;
		try{
			//1.粗选POI:根据operationResult解析获取要批引导link的poi数据
			if(opResult.getAllObjs().size()==0){
				log.info("没有获取到有变更的poi数据");
				return "";
				}
			
			List<Long> pids=new ArrayList<Long>();
			//2.把精选的POI.pid放在临时表temp_poi_glink_yyyyMMddhhmmss（临时表不存在则新建）；
			String tempPoiGLinkTab = createTempPoiGLinkTable(conn);
			//3.精选POI:根据粗选的结果，进一步过滤得到(新增POI或修改引导坐标或引导link为0的POI对象或对应引导link不存在rd_link表中)
			Set<Long> refinedPois = new HashSet<Long>();
			for(BasicObj poiObj:opResult.getAllObjs()){
				pids.add(poiObj.objPid());
				if(OperationType.INSERT==poiObj.getMainrow().getHisOpType()||
						(OperationType.UPDATE==poiObj.getMainrow().getHisOpType()&&(poiObj.getMainrow().hisOldValueContains(IxPoi.Y_GUIDE)
								||poiObj.getMainrow().hisOldValueContains(IxPoi.X_GUIDE)||
								Integer.valueOf(0).equals(poiObj.getMainrow().getAttrByColName("LINK_PID"))))){
					refinedPois.add(poiObj.objPid());
				}
			}
			count=count+insertPois2TempTab(refinedPois,tempPoiGLinkTab,conn);
			count=count+insertPoisNotInRdLink2TempTab(CollectionUtils.subtract(pids, refinedPois),tempPoiGLinkTab,conn);
			if(count==0){return "";}
			return tempPoiGLinkTab;
		}catch(Exception e){
			log.info(e.getMessage());
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	private int insertPoisNotInRdLink2TempTab(Collection<Long> pids,String tempPoiTable,Connection conn) throws Exception {
		String sql = "insert  into "+tempPoiTable
				+ " select pid from ix_poi t  "
				+ " where t.pid in (select column_value from table(clob_to_table(?))) "
				+ " and not exists(select 1 from rd_link r where r.link_pid=t.link_pid)";
		this.log.debug("sql:"+sql);
		Clob clobPids=ConnectionUtil.createClob(conn);
		clobPids.setString(1, StringUtils.join(pids, ","));
		return new QueryRunner().update(conn, sql, clobPids);
	}
	private int insertPois2TempTab(Collection<Long> pids,String tempPoiTable,Connection conn) throws Exception{
		String sql = "insert into "+tempPoiTable
				+ " select column_value from table(clob_to_table(?)) ";
		this.log.debug("sql:"+sql);
		Clob clobPids=ConnectionUtil.createClob(conn);
		clobPids.setString(1, StringUtils.join(pids, ","));
		return new  QueryRunner().update(conn, sql, clobPids);
	}
	private String createTempPoiGLinkTable(Connection conn) throws Exception {
		String tableName = "tmp_p_glink"+(new SimpleDateFormat("yyyyMMddhhmmssS").format(new Date()));
		String sql ="create table "+tableName+" (pid number(10))";
		new QueryRunner().update(conn, sql);
		return tableName;
	}
	protected void updateField(OperationResult opResult,Connection conn) throws Exception {
		List<Integer> pids=new ArrayList<Integer>();
		for(BasicObj Obj:opResult.getAllObjs()){
			IxPoi ixPoi = (IxPoi) Obj.getMainrow();
			Integer pid=(int) ixPoi.getPid();
			pids.add(pid);
		}
		if(pids==null||pids.size()<=0){return;}
		
		List<Object> values = new ArrayList<Object> ();
		SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,pids," IP.PID  ");
		values.addAll(inClause.getValues());

		log.info("开始所有记录更新MESH_ID_5K、ROAD_FLAG");
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE IX_POI IP  SET IP.MESH_ID_5K = NAVI_GEOM.GET5KMAPNUMBER1(ip.GEOMETRY, ip.MESH_ID),IP.ROAD_FLAG  = '0'\r\n");
		sb .append(" WHERE "+ inClause.getSql());
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		log.info("MESH_ID_5K、ROAD_FLAG:"+sqlClause);
		int count = sqlClause.update(conn);

		log.info("开始更新PMESH_ID");
		StringBuilder sb1 = new StringBuilder();
		sb1.append("MERGE INTO IX_POI P\r\n" + 
				" USING (SELECT IP.PID, R.MESH_ID\r\n" + 
				"          FROM IX_POI IP, RD_LINK R\r\n" + 
				"        WHERE IP.LINK_PID = R.LINK_PID\r\n" + 
				"          AND IP.PMESH_ID=0 \r\n" + 
				"          AND R.MESH_ID<>0 \r\n");
		sb1 .append(" AND "+ inClause.getSql());
		sb1.append(") T ON (P.PID = T.PID)\r\n" +
					"WHEN MATCHED THEN\r\n" +
					"  UPDATE SET P.PMESH_ID = T.MESH_ID\r\n");
		SqlClause sqlClause1 = new SqlClause(sb1.toString(),values);
		log.info("PMESH_ID:"+sqlClause1);
		int count1 = sqlClause1.update(conn);
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
		tabNames.add("IX_POI_PARKING");
		tabNames.add("IX_POI_PHOTO");
		tabNames.add("IX_POI_CARRENTAL");
		tabNames.add("IX_POI_HOTEL");
		tabNames.add("IX_POI_NAME_FLAG");
		OperationResult result = new OperationResult();
		Map<Long,BasicObj> objs =  ObjBatchSelector.selectByPids(monthConn, "IX_POI", tabNames,false, logStatInfo.keySet(), true, true);
		ObjHisLogParser.parse(objs,logStatInfo);
		result.putAll(objs.values());
		return result;
	}

	private FmDay2MonSync createSyncInfo(Day2MonthSyncApi d2mSyncApi, int regionId, Date syncTimeStamp) throws Exception {
		FmDay2MonSync info = new FmDay2MonSync();
		info.setRegionId(regionId);
		info.setSyncStatus(FmDay2MonSync.SyncStatusEnum.CREATE.getValue());
		info.setJobId(this.getJobInfo().getId());
		Long sid = d2mSyncApi.insertSyncInfo(info );//写入本次的同步信息
		info.setSid(sid);
		info.setSyncTime(syncTimeStamp);
		return info;
	}
	//加锁的扩圈都放在DMS，DMS查和写都扩圈，所以FM查和写都不用扩圈
	private void dealFmAndDMSLock(OracleSchema monthDbSchema,List<Integer> meshs) throws Exception {
		Connection monthConn = monthDbSchema.getPoolDataSource().getConnection();
		//获取锁
		String sql = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE  LOCK_OWNER='GLOBAL' FOR UPDATE";
		Statement sourceStmt = monthConn.createStatement();
		try{
			ResultSet rs = sourceStmt.executeQuery(sql);
			String sqlMeshAll = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE FGM.MESH_ID = 0 ";
			ResultSet rss = sourceStmt.executeQuery(sqlMeshAll);
			int lockStatus=0;
			while(rss.next()){lockStatus=rss.getInt("LOCK_STATUS");}
			if(lockStatus==1){throw new Exception("DMS全库加锁");}
			log.info("判断是否有图幅锁");
			hasLock(monthConn,meshs);
			log.info("无锁，则图幅加锁");
			getMeshLock(monthConn,meshs);
		}catch(Exception e){
			if(monthConn!=null)monthConn.rollback();
			log.info("加锁图幅回滚");
			throw e;
			}
		finally{
			DbUtils.commitAndCloseQuietly(monthConn);
		}
	}
	private void releaseMeshLock(Connection monthConn,List<Integer> meshs) throws Exception {
		if (meshs == null || meshs.size() == 0) {return;}
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		//获取锁
		String sql = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE  LOCK_OWNER='GLOBAL' FOR UPDATE";
		try{
			run.query(monthConn,sql,new ColumnListHandler("LOCK_STATUS"));//获取锁
			sb.append("DELETE FROM FM_GEN2_MESHLOCK WHERE LOCK_STATUS=1 AND LOCK_OWNER='FM'");
			
			List<Object> values = new ArrayList<Object>();
			
			if (meshs.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(monthConn);
				clob.setString(1, StringUtils.join(meshs, ","));
				sb.append(" AND MESH_ID in (select column_value from table(clob_to_table(?))) ");
				values.add(clob);
			} else {
				sb.append(" AND MESH_ID IN (" + StringUtils.join(meshs, ",") + ")");
			}
			if (values != null && values.size() > 0) {
				Object[] queryValues = new Object[values.size()];
				for (int i = 0; i < values.size(); i++) {
					queryValues[i] = values.get(i);
				}
				run.update(monthConn, sb.toString(), queryValues);
			} else {
				run.update(monthConn, sb.toString());
			}
			
		}catch(Exception e){
			if(monthConn!=null)monthConn.rollback();
			log.info("加锁图幅回滚");
			throw e;
			}
		finally{
			DbUtils.commitAndCloseQuietly(monthConn);
		}
		
	}
	private void getMeshLock(Connection monthConn,List<Integer> meshs) throws Exception {
		Statement stmt = monthConn.createStatement();
		try{
			for(int m:meshs){
				String sql = "INSERT INTO FM_GEN2_MESHLOCK (MESH_ID,LOCK_STATUS ,LOCK_OWNER,JOB_ID) VALUES ("+ m +", 1,'FM','"+this.getJobInfo().getId()+"') ";
				stmt.addBatch(sql);
			}
			stmt.executeBatch();
		}catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DbUtils.close(stmt);
		}
	}
	private void hasLock(Connection monthConn,List<Integer> meshs) throws Exception {
		Statement sourceStmt = monthConn.createStatement();
		String sql = "SELECT FGM.MESH_ID FROM FM_GEN2_MESHLOCK FGM WHERE FGM.LOCK_OWNER='GEN2' AND FGM.LOCK_STATUS=1 ";
		ResultSet rs = sourceStmt.executeQuery(sql);
		List<Integer> gdbMeshs = new ArrayList<Integer>();
		while(rs.next()){
			gdbMeshs.add(rs.getInt("MESH_ID"));
		}
		List<Integer> retainMeshs = new ArrayList<>(meshs);
		retainMeshs.retainAll(gdbMeshs);
		if(retainMeshs!=null&&retainMeshs.size()>0){
			throw new Exception("以下图幅DMS加锁:"+retainMeshs.toString());
		}
	}
	
	protected List<Integer> selectLogGridsByTaskId(List<Integer> taskIds,DatahubApi datahubApi,Region region,int subTaskType) throws Exception{
	
		DbInfo dailyDbInfo = datahubApi.getDbById(region.getDailyDbId());
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo.getConnectParam()));
		Connection conn = dailyDbSchema.getPoolDataSource().getConnection();
		
		try{
			QueryRunner queryRunner = new QueryRunner();
			SqlClause sqlClause = getSelectLogSql(conn,taskIds,subTaskType);
			
			ResultSetHandler<List<Integer>> rsh = new ResultSetHandler<List<Integer>>() {
				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Integer> msgs = new ArrayList<Integer>();
					while(rs.next()){
						msgs.add(rs.getInt("GRID_ID"));
					}
					return msgs;
				}
			};
			List<Integer> query = queryRunner.query(conn, sqlClause.getSql(), rsh, sqlClause.getValues().toArray());
			return query;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	protected SqlClause getSelectLogSql(Connection conn,List<Integer> grids) throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append(" select distinct g.GRID_ID\r\n" + 
				"   from log_operation   p,\r\n" + 
				"       log_detail d,\r\n" +
				"       log_detail_grid g,\r\n" + 
				"       poi_edit_status s\r\n" + 
				"   where p.op_id = d.op_id\r\n" + 
				"    and d.row_id = g.log_row_id\r\n" + 
				"    and (d.ob_pid = s.pid or d.geo_pid = s.pid)\r\n"+ 
				"    and p.com_sta = 0"+ 
				"    and (d.ob_nm = 'IX_POI' or d.geo_nm = 'IX_POI')"+ 
				"    and s.status = 3");
				 
		List<Object> values = new ArrayList<Object> ();
		if(grids!=null&&grids.size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,grids," g.GRID_ID ");
			if (inClause!=null){
				sb .append(" AND "+ inClause.getSql());
				values.addAll(inClause.getValues());
			}
		}
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	protected SqlClause getSelectLogSql(Connection conn,List<Integer> taskIds, int taskType) throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append(" select /*+ leading(P,D,G,S)*/  distinct g.GRID_ID\r\n" + 
				"   from log_operation   p,\r\n" + 
				"       log_detail d,\r\n" +
				"       log_detail_grid g,\r\n" + 
				"       poi_edit_status s\r\n" + 
				"   where p.op_id = d.op_id\r\n" + 
				"    and d.row_id = g.log_row_id\r\n" + 
				"    and (d.ob_pid = s.pid or d.geo_pid = s.pid)\r\n"+ 
				"    and p.com_sta = 0"+ 
				"    and (d.ob_nm = 'IX_POI' or d.geo_nm = 'IX_POI')"+ 
				"    and s.status = 3");
		if(taskType==0&&(taskIds==null||taskIds.size()==0)){
			sb.append(" and s.medium_task_id<>0 ");
		}
				 
		List<Object> values = new ArrayList<Object> ();
		if(taskIds!=null&&taskIds.size()>0){
			String str=" s.medium_task_id ";
			if(taskType==1){
				str=" s.quick_task_id ";
			}
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,taskIds,str);
			if (inClause!=null){
				sb .append(" AND "+ inClause.getSql());
				values.addAll(inClause.getValues());
			}
			
		}
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		log.info("查询存在履历的grids:"+sqlClause.getSql());
		return sqlClause;
	}
	
	private List<Integer> grids2meshs(List<Integer> grids) throws Exception {
		List<Integer> meshs =new ArrayList<Integer>();
		for(int g:grids){
			int mesh=(int) Math.floor(g/100);
			if(meshs.contains(mesh)){
				continue;
			}
			meshs.add(mesh);
		}
		return meshs;
	}
	
	private List<Integer> meshs2grids(List<Integer> meshs) throws Exception {
		List<Integer> grids =new ArrayList<Integer>();
		for(Object obj:meshs){
			int m=0;
			if(obj instanceof Integer){  
				m=(int) obj;
			}else if(obj instanceof String){  
				m =Integer.parseInt(String.valueOf(obj));
            }
			for(int i=0;i<4;i++){
				for(int j=0;j<4;j++){
					grids.add(m*100 + i*10+ j);
				}
			}
		}
		return grids;
	}
	
	private void callDmsReleaseLockApi(long jobId) throws IOException {
		JSONObject parameter = new JSONObject();
		try {
			Map<String,String> parMap=new HashMap<String,String>();
			parameter.put("jobId", jobId);
			parMap.put("parameter", parameter.toString());
			ServiceInvokeUtil http =new ServiceInvokeUtil();
			//String msUrl ="http://192.168.3.228:8086/VMWeb/springmvc/vmmanager/day2mounth/releaseLock?";
			String msUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.day2mounthReleaseLock);
			String json=http.invoke(msUrl,parMap,1000);
			log.info("调用DMS解锁接口:"+json);
		 } catch (Exception e) {
			 log.debug("调用DMS解锁接口:"+e.getMessage());
			 //throw new IOException(e);
	     } 
	}
	private Map<Integer,String> getDmsLock(List<Integer> meshes,long jobId,DbInfo dbInfos) throws IOException {
		Map<Integer,String> dmsLockMeshes = new HashMap<Integer,String>();
		if(meshes==null||meshes.size()==0){
			return dmsLockMeshes;
		}
		JSONObject parameter = new JSONObject();
		JSONObject dbInfo = new JSONObject();
		try {
			dbInfo.put("schemaName", dbInfos.getDbUserName());
			dbInfo.put("ip", dbInfos.getDbServer().getIp());
			parameter.put("jobId", jobId);
			parameter.put("dbInfo", dbInfo);
			parameter.put("meshIds", meshes);
			JSONObject jsonReq = JSONObject.fromObject(callDmsGetLockApi(parameter));
			if(jsonReq.getInt("errcode")==0){
				if(jsonReq.get("data")!=null){
					dmsLockMeshes =(Map<Integer,String>) jsonReq.get("data");
				}
			}
			log.info("DMS被锁图幅:"+dmsLockMeshes);		
		} catch (Exception e) {
			log.debug("调用DMS加锁接口:"+e.getMessage());
			throw new IOException(e);
	    } 
		return dmsLockMeshes;
	}
	
	private String callDmsGetLockApi(JSONObject parameter) throws IOException {
		String json="";
		try {
			Map<String,String> parMap=new HashMap<String,String>();
			parMap.put("parameter", parameter.toString());
			ServiceInvokeUtil http =new ServiceInvokeUtil();
			//String msUrl ="http://192.168.3.228:8086/VMWeb/springmvc/vmmanager/day2mounth/getLock?";
			String msUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.day2mounthGetLock);
			json=http.invoke(msUrl,parMap,1000);
			log.info("调用DMS加锁接口:"+json);
		 } catch (Exception e) {
			 log.debug("调用DMS加锁接口:"+e.getMessage());
			 throw new IOException(e);
	     } 
		return json;
	}
	
	public static void main(String[] args) throws JobException{
		new Day2MonthPoiMergeJob(null).execute();
	}

}
