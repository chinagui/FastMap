package com.navinfo.dataservice.column.job;

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
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.DateUtils;
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
		MetadataApi metaApi = (MetadataApi)ApplicationContextUtil
				.getBean("metadataApi");
		try {
			Day2MonthPoiMergeJobRequest day2MonRequest=(Day2MonthPoiMergeJobRequest) request;
			int specRegionId = day2MonRequest.getSpecRegionId();
			List<Integer> specMeshes = day2MonRequest.getSpecMeshes();
			int phaseId = day2MonRequest.getPhaseId();

			//确定需要日落月的大区
			List<Region> regions = null;
			if(specRegionId>0){//判断是否有指定大区的日落月
				Region r = manApi.queryByRegionId(specRegionId);
				regions = new ArrayList<Region>();
				regions.add(r);
			}else{//全部大区
				regions = manApi.queryRegionList();
			}
			log.info("确定日落月大区库个数："+regions.size()+"个。");
			response("确定日落月大区库个数："+regions.size()+"个。",null);
			
			List<Integer> grids = new ArrayList<Integer>();
			//支持精编任务关闭日落月
			if(specMeshes!=null&&specMeshes.size()>0){
				grids= meshs2grids(specMeshes);
				for(Region region:regions){
					
					//20170426 按任务落添加图幅开关判断
					//	1、获取任务范围内已关闭的图幅号
					List<Integer> closemeshes = metaApi.getCloseMeshs(specMeshes);
					//	2、将任务范围内关闭的图幅号转换成grids
					List<Integer> closeGrids = meshs2grids(closemeshes);
					//	3、筛选这些grids中的是否存在未落的履历
					if(closeGrids.size()>0){
						List<String> logGrids =selectLogFromCloseGrids(closeGrids,datahubApi,region);
						if(logGrids!=null&&logGrids.size()>0){
							throw new Exception("以下gird存在需要落的数据，但是对应的图幅关闭，请开启后再落:"+logGrids.toString());
						}
					}
					
					doSync(region,null,grids, datahubApi, d2mSyncApi,manApi,phaseId);
					log.info("大区库（regionId:"+region.getRegionId()+"）日落月完成。");
				}
			//支持每天定时日落月	
			}else{
				//获取region对应的省份
				List<CpRegionProvince> regionProvs = manApi.listCpRegionProvince();
				Map<Integer,Set<Integer>> adminMap = new HashMap<Integer,Set<Integer>>();
				for(CpRegionProvince cp:regionProvs){
					if(adminMap.containsKey(cp.getRegionId())){
						adminMap.get(cp.getRegionId()).add(cp.getAdmincode());
					}else{
						Set<Integer> codes = new HashSet<Integer>();
						codes.add(cp.getAdmincode());
						adminMap.put(cp.getRegionId(), codes);
					}
				}
				//开始分配
				for(Region region:regions){
					//获取region包含的省份
					Set<Integer> admins = adminMap.get(region.getRegionId());
					//过去大区库内的关闭图幅并转换成girds
					List<Mesh4Partition> meshes = metaApi.queryMeshes4PartitionByAdmincodes(admins);
					List<Integer> filterGrids = new ArrayList<Integer>();
					for(Mesh4Partition m:meshes){
						if(m.getDay2monSwitch()==0){
							int mId = m.getMesh();
							for(int i=0;i<4;i++){
								for(int j=0;j<4;j++){
									filterGrids.add(mId*100 + i*10+ j);
								}
							}
						}
					}
					doSync(region,filterGrids,grids,datahubApi, d2mSyncApi,manApi,phaseId);
					log.info("大区库（regionId:"+region.getRegionId()+"）日落月完成。");
				}
			}
			

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
			
		}
		

	}
	

	private void doSync(Region region,List<Integer> filterGrids,List<Integer> grids,DatahubApi datahubApi, Day2MonthSyncApi d2mSyncApi,ManApi manApi,int phaseId)
			throws Exception{
		
		//1. 获取最新的成功同步信息，并记录本次同步信息
		FmDay2MonSync lastSyncInfo = d2mSyncApi.queryLastedSyncInfo(region.getRegionId());
		log.info("获取最新的成功同步信息："+lastSyncInfo);		
		Date syncTimeStamp= new Date();
		FmDay2MonSync curSyncInfo = createSyncInfo(d2mSyncApi, region.getRegionId(),syncTimeStamp);//记录本次的同步信息
		d2mSyncApi.insertSyncInfo(curSyncInfo);
		
		log.info("获取大区库连接信息:"+region);
		if(region==null) return ;
		Integer dailyDbId = region.getDailyDbId();
		DbInfo dailyDbInfo = datahubApi.getDbById(dailyDbId);
		log.info("获取dailyDbInfo信息:"+dailyDbInfo);
		Integer monthDbId = region.getMonthlyDbId();
		DbInfo monthDbInfo = datahubApi.getDbById(monthDbId);
		log.info("获取monthDbInfo信息:"+monthDbInfo);		
		
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo.getConnectParam()));
		Connection dailyConn = dailyDbSchema.getPoolDataSource().getConnection();
		OracleSchema monthDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(monthDbInfo.getConnectParam()));
		Connection monthConn = monthDbSchema.getPoolDataSource().getConnection();
		LogMover logMover = null;
		LogSelector logSelector = null;
		List<Integer> allGrids =new ArrayList<Integer>();
		
		if(filterGrids!=null&&filterGrids.size()>0){allGrids = filterGrids;}
		if(grids!=null&&grids.size()>0){allGrids = grids;}
		List<Integer> meshs =grids2meshs(allGrids);
		String tempPoiGLinkTab ="";
		boolean isbatch = true;
		OperationResult result=new OperationResult();
		try{
			
			log.info("处理日落月和DMS的数据读写锁");
			dealFmAndDMSLock(monthDbSchema,meshs);
			
			log.info("开始获取日编库履历");
			String tempOpTable="";
			if(grids!=null&&grids.size()>0){
				logSelector = new Day2MonPoiLogSelector(dailyDbSchema,syncTimeStamp,grids);
				tempOpTable = logSelector.select();
			}else{
				logSelector = new Day2MonPoiLogByFilterGridsSelector(dailyDbSchema,syncTimeStamp,filterGrids);
				tempOpTable = logSelector.select();
			}
			
			log.info("开始将日库履历刷新到月库,temptable:"+tempOpTable);
			FlushResult flushResult= new Day2MonLogFlusher(dailyDbSchema,dailyConn,monthConn,true,tempOpTable,"day2MonSync").flush();
			if(0==flushResult.getTotal()){
				log.info("没有符合条件的履历，不执行日落月，返回");
				isbatch = false;
			}else{
				log.info("开始将履历搬到月库：logtotal:"+flushResult.getTotal());
				logMover = new Day2MonMover(dailyDbSchema, monthDbSchema, tempOpTable, flushResult.getTempFailLogTable());
				LogMoveResult logMoveResult = logMover.move();
				log.info("开始进行履历分析");
				result = parseLog(logMoveResult, monthConn);
				if(result.getAllObjs().size()==0){throw new LockException("可落的履历全部刷库失败，请查看："+flushResult.getTempFailLogTable());}
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
				
				updateLogCommitStatus(dailyConn,tempOpTable);
				
			}
			log.info("修改同步信息为成功");
			curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.SUCCESS.getValue());
			d2mSyncApi.updateSyncInfo(curSyncInfo);
			log.info("finished:"+region.getRegionId());
			
			//更新任务状态
			if(grids!=null&&grids.size()>0){
				manApi.taskUpdateCmsProgress(phaseId,2,null);
			}
			
			log.info("开始筛选需要批引导LINK的POI");
			tempPoiGLinkTab=createPoiTabForBatchGL(result,monthDbSchema);
			log.info("需要执行引导LINK批处理的POI在临时表中："+tempPoiGLinkTab);
			
		}catch(Exception e){
			isbatch = false;
			if(monthConn!=null)monthConn.rollback();
			if(dailyConn!=null)dailyConn.rollback();
			log.info("rollback db");
			try{
				curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.FAIL.getValue());
				d2mSyncApi.updateSyncInfo(curSyncInfo);
				//更新任务状态
				if(grids!=null&&grids.size()>0){
					manApi.taskUpdateCmsProgress(phaseId,3,"日落月脚本错误："+e.getMessage());
				}
			}catch(Exception ee){
				log.info("回滚任务状态报错："+ee.getMessage());
			}
			
			if(logMover!=null){
				log.info("搬移履历回滚");
				logMover.rollbackMove();
			}
			throw e;
			
		}finally{
			log.info("释放加锁图幅");
			releaseMeshLock(monthConn,meshs);
			DbUtils.commitAndCloseQuietly(monthConn);
			DbUtils.commitAndCloseQuietly(dailyConn);
			log.info("commit db");
			if(logSelector!=null){
				log.info("释放履历锁");
				logSelector.unselect(false);
			}
			if(isbatch&&!tempPoiGLinkTab.isEmpty()){
				log.info("开始执行引导LINK批处理");
				new PoiGuideLinkBatch(tempPoiGLinkTab,monthDbSchema).execute();
				log.info("引导LINK批处理执行完成");
			}
			
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
		//获取锁
		String sql = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE  LOCK_OWNER='GLOBAL' FOR UPDATE";
		Statement sourceStmt = monthConn.createStatement();
		try{
			ResultSet rs = sourceStmt.executeQuery(sql);//获取锁
			QueryRunner run = new QueryRunner();
			for(int m:meshs){
				String sqlD = "DELETE FROM FM_GEN2_MESHLOCK WHERE MESH_ID="+ m +" AND LOCK_STATUS=1 AND LOCK_OWNER='FM'";
				run.execute(monthConn, sqlD);
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
		QueryRunner run = new QueryRunner();
		for(int m:meshs){
			String sql = "INSERT INTO FM_GEN2_MESHLOCK (MESH_ID,LOCK_STATUS ,LOCK_OWNER,JOB_ID) VALUES ("+ m +", 1,'FM','"+this.getJobInfo().getId()+"')";
			run.execute(monthConn, sql);
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
	
	protected List<String> selectLogFromCloseGrids(List<Integer> grids,DatahubApi datahubApi,Region region) throws Exception{
	
		DbInfo dailyDbInfo = datahubApi.getDbById(region.getDailyDbId());
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo.getConnectParam()));
		Connection conn = dailyDbSchema.getPoolDataSource().getConnection();
		
		try{
			QueryRunner queryRunner = new QueryRunner();
			SqlClause sqlClause = getSelectLogSql(conn,grids);
			
			ResultSetHandler<List<String>> rsh = new ResultSetHandler<List<String>>() {
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<String> msgs = new ArrayList<String>();
					while(rs.next()){
						msgs.add(rs.getString("GRID_ID"));
					}
					return msgs;
				}
			};
			List<String> query = queryRunner.query(conn, sqlClause.getSql(), rsh, sqlClause.getValues().toArray());
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
			if (inClause!=null)
				sb .append(" AND "+ inClause.getSql());
			values.addAll(inClause.getValues());
		}
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
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
		for(int m:meshs){
			for(int i=0;i<4;i++){
				for(int j=0;j<4;j++){
					grids.add(m*100 + i*10+ j);
				}
			}
		}
		return grids;
	}
	
	public static void main(String[] args) throws JobException{
		new Day2MonthPoiMergeJob(null).execute();
	}

}
