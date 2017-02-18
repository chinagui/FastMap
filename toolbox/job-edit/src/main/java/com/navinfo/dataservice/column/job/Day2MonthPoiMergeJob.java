package com.navinfo.dataservice.column.job;

import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
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
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationResultException;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.day2mon.Check;
import com.navinfo.dataservice.day2mon.Classifier;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogByFilterGridsSelector;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogSelector;
import com.navinfo.dataservice.day2mon.DeepInfoMarker;
import com.navinfo.dataservice.day2mon.PostBatch;
import com.navinfo.dataservice.day2mon.PreBatch;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogFlusher;
import com.navinfo.dataservice.impcore.mover.Day2MonMover;
import com.navinfo.dataservice.impcore.mover.LogMoveResult;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
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
				for(Integer m:specMeshes){
					for(int i=0;i<4;i++){
						for(int j=0;j<4;j++){
							grids.add(m*100 + i*10+ j);
						}
					}
				}
				for(Region region:regions){
					doSync(region,null,grids, datahubApi, d2mSyncApi);
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
					doSync(region,filterGrids,grids,datahubApi, d2mSyncApi);
					log.info("大区库（regionId:"+region.getRegionId()+"）日落月完成。");
				}
			}
			

		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
			
		}
		

	}

	private void doSync(Region region,List<Integer> filterGrids,List<Integer> grids,DatahubApi datahubApi, Day2MonthSyncApi d2mSyncApi)
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
		
		try{
			
			log.info("处理日落月和DMS的数据读写锁");
			dealFmAndDMSLock(monthDbSchema,meshs);
			
			log.info("开始获取日编库履历");
			String tempOpTable="";
			if(filterGrids!=null&&filterGrids.size()>0){
				logSelector = new Day2MonPoiLogByFilterGridsSelector(dailyDbSchema,syncTimeStamp,filterGrids);
				tempOpTable = logSelector.select();
			}
			if(grids!=null&&grids.size()>0){
				logSelector = new Day2MonPoiLogSelector(dailyDbSchema,syncTimeStamp,grids);
				tempOpTable = logSelector.select();
			}
			
			log.info("开始将日库履历刷新到月库,temptable:"+tempOpTable);
			FlushResult flushResult= new Day2MonLogFlusher(dailyDbSchema,dailyConn,monthConn,true,tempOpTable).flush();
			if(0==flushResult.getTotal()){
				log.info("没有符合条件的履历，不执行日落月，返回");
				return;
			}else{
				log.info("开始将履历搬到月库：logtotal:"+flushResult.getTotal());
				logMover = new Day2MonMover(dailyDbSchema, monthDbSchema, tempOpTable, flushResult.getTempFailLogTable());
				LogMoveResult logMoveResult = logMover.move();
				log.info("开始进行履历分析");
				OperationResult result = parseLog(logMoveResult, monthConn);
				log.info("开始进行深度信息打标记");
				new DeepInfoMarker(result,monthConn).execute();
				log.info("开始执行前批");
				new PreBatch(result, monthConn).execute();
				log.info("开始执行检查");
				Map<String, Map<Long, Set<String>>> checkResult = new Check(result, monthConn).execute();
				new Classifier(checkResult,monthConn).execute();
				log.info("开始执行后批处理");
				new PostBatch(result,monthConn).execute();
				updateLogCommitStatus(dailyConn,tempOpTable);
			}
			log.info("修改同步信息为成功");
			curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.SUCCESS.getValue());
			d2mSyncApi.updateSyncInfo(curSyncInfo);
			log.info("finished:"+region.getRegionId());
		}catch(Exception e){
			if(monthConn!=null)monthConn.rollback();
			if(dailyConn!=null)dailyConn.rollback();
			log.info("rollback db");
			curSyncInfo.setSyncStatus(FmDay2MonSync.SyncStatusEnum.FAIL.getValue());
			d2mSyncApi.updateSyncInfo(curSyncInfo);
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
	
	private void dealFmAndDMSLock(OracleSchema monthDbSchema,List<Integer> meshs) throws Exception {
		Connection monthConn = monthDbSchema.getPoolDataSource().getConnection();
		String sql = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE FGM.MESH_ID = 0 FOR UPDATE";
		Statement sourceStmt = monthConn.createStatement();
		try{
			ResultSet rs = sourceStmt.executeQuery(sql);
			int lockStatus=0;
			while(rs.next()){lockStatus=rs.getInt("LOCK_STATUS");}
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
		QueryRunner run = new QueryRunner();
		for(int m:meshs){
			String sql = "DELETE FROM FM_GEN2_MESHLOCK WHERE MESH_ID="+ m +" AND LOCK_STATUS=1 AND LOCK_OWNER='FM'";
			run.execute(monthConn, sql);
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
		String sql = "SELECT FGM.MESH_ID FROM FM_GEN2_MESHLOCK FGM WHERE FGM.LOCK_OWNER='GDB' AND FGM.LOCK_STATUS=1 ";
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
	
	public static void main(String[] args) throws JobException{
		new Day2MonthPoiMergeJob(null).execute();
	}

}
