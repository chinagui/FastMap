package com.navinfo.dataservice.column.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.Day2MonthSyncApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationResultException;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.day2mon.*;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogFlusher;
import com.navinfo.dataservice.impcore.flusher.Day2MonLogMultiFlusher;
import com.navinfo.dataservice.impcore.mover.Day2MonMover;
import com.navinfo.dataservice.impcore.mover.LogMoveResult;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @ClassName: Day2MonthPoiMergeJob
 * @author MaYunFei
 * @date 下午8:27:56
 * @Description: POI 日落月融合Job 1.找到那些开关属于打开状态的城市
 *               2.按照大区将这些城市进行分组；key=cityid，value=regionid; 3.按city开始找满足条件的履历
 *               4.分析履历，增加、修改的poi要放到OperationResult 的列表中，为后续的精编批处理、检查做准备
 *               5.将3得到的履历刷到月库； 6.搬履历到月库； 7.月库执行精编批处理检查：根据4得到的OperationResult
 *               8.月库打重分类的标记 9.深度信息打标记； 10.修改day_mon_sync 状态为成功 11.按照城市统计日落月的数据量
 */
public class Day2MonthPoiMerge915TmpJob extends AbstractJob {
	private List<LogMover> logMovers = new ArrayList<LogMover>();

	public Day2MonthPoiMerge915TmpJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		DatahubApi datahubApi = (DatahubApi) ApplicationContextUtil
				.getBean("datahubApi");
		Day2MonthSyncApi d2mSyncApi = (Day2MonthSyncApi) ApplicationContextUtil
				.getBean("day2MonthSyncApi");
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil
				.getBean("metadataApi");
		try {
			Day2MonthPoiMerge915TmpJobRequest day2MonRequest = (Day2MonthPoiMerge915TmpJobRequest) request;
			String tmpOpTable = day2MonRequest.getTmpOpTable();// 日库临时表
			String tempFailLogTable = day2MonRequest.getTempFailLogTable();// 日库失败履历临时表
			int onlyFlushLog = day2MonRequest.getOnlyFlushLog();// 日库失败履历临时表
			int specRegionId = day2MonRequest.getSpecRegionId();

			DbInfo dbInfo = datahubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS
					.getValue());

			// 确定需要日落月的大区
			List<Region> regions = new ArrayList<Region>();

			Region r = manApi.queryByRegionId(specRegionId);
			regions.add(r);

			log.info("确定日落月大区库个数：" + regions.size() + "个。");
			response("确定日落月大区库个数：" + regions.size() + "个。", null);

			try {
				for (Region region : regions) {

					/**
					 * 中线任务 1）每天定时落 ①获取所有的大区库，依次日落月每个大区库
					 * ②每个大区库中查询：粗编完成且图幅开关为开启的数据履历信息； ③筛选到履历后，获取所有的grids;
					 * ④将所有的grids转换成对应的meshes； ⑤拿所有的meshes申请DMS锁；
					 * ⑥若存在申请不到DMS锁的图幅，则报出具体图幅被锁信息，申请到锁的图幅执行日落月
					 */
					if (StringUtils.isNotEmpty(tmpOpTable)
							&& StringUtils.isNotEmpty(tempFailLogTable)) {
						doMediumSync(region, null, null, null, datahubApi,
								d2mSyncApi, manApi, tmpOpTable,
								tempFailLogTable, 0);
					} else {
						List<Integer> filterGrids = new ArrayList<Integer>();
						List<Integer> logGrids = selectLogGridsByTaskId(null,
								datahubApi, region, 0);// 查询所有存在可落履历的grids
						List<Integer> logMeshes = grids2meshs(logGrids);
						List<Integer> closeMeshes = metaApi
								.getMeshsFromPartition(logMeshes, 0, 0);// 查询关闭的图幅

						filterGrids.addAll(meshs2grids(closeMeshes));
						log.info("以下关闭图幅内的数据履历未日落月：" + closeMeshes.toString());

						logMeshes.removeAll(closeMeshes);// 拿所有的未关闭的meshes申请DMS锁；
						Map<Integer, String> dmsLockInfo = new HashMap<Integer, String>();
						dmsLockInfo = getDmsLock(logMeshes, jobInfo.getId(),
								dbInfo);

						List<Integer> dmsLockMeshes = new ArrayList<Integer>();
						dmsLockMeshes.addAll(dmsLockInfo.keySet());

						filterGrids.addAll(meshs2grids(dmsLockMeshes));
						log.info("以下未申请到DMS锁的图幅，未日落月："
								+ dmsLockMeshes.toString());

						doMediumSync(region, filterGrids, null, null,
								datahubApi, d2mSyncApi, manApi, null, null,
								onlyFlushLog);
					}
				}

			} catch (Exception e) {
				callDmsReleaseLockApi(jobInfo.getId());
				log.error(e.getMessage(), e);
				throw new JobException(e.getMessage(), e);
			}

			callDmsReleaseLockApi(jobInfo.getId());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}

	}

	private Connection getConnectByRegion(Region region, DatahubApi datahubApi,
			String flag) throws Exception {
		log.info("获取大区库连接信息:" + region);
		Integer dbId = 0;
		if (flag.equals("daily")) {
			dbId = region.getDailyDbId();
			log.info("获取日大区库dbId:" + dbId);
		} else {
			dbId = region.getMonthlyDbId();
			log.info("获取月大区库dbId:" + dbId);
		}
		DbInfo dbInfo = datahubApi.getDbById(dbId);
		log.info("获取数据库信息:" + dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		Connection conn = dbSchema.getPoolDataSource().getConnection();
		return conn;
	}

	private OracleSchema getSchemaByRegion(Region region,
			DatahubApi datahubApi, String flag) throws Exception {
		log.info("获取大区库连接信息:" + region);
		Integer dbId = 0;
		if (flag.equals("daily")) {
			dbId = region.getDailyDbId();
			log.info("获取日大区库dbId:" + dbId);
		} else {
			dbId = region.getMonthlyDbId();
			log.info("获取月大区库dbId:" + dbId);
		}
		DbInfo dbInfo = datahubApi.getDbById(dbId);
		log.info("获取数据库信息:" + dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		return dbSchema;
	}

	private OracleSchema getGdbSchema(DatahubApi databhubApi) throws Exception {
		DbInfo dbInfo = databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS
				.getValue());
		log.info("获GDB母库连接信息:" + dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		return dbSchema;
	}

	private Connection getGdbConnect(DatahubApi databhubApi) throws Exception {
		DbInfo dbInfo = databhubApi.getOnlyDbByType(DbInfo.BIZ_TYPE.GDB_PLUS
				.getValue());
		log.info("获GDB母库连接信息:" + dbInfo);
		OracleSchema dbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		Connection conn = dbSchema.getPoolDataSource().getConnection();
		return conn;
	}

	private void doMediumSync(Region region, List<Integer> filterGrids,
			List<Integer> grids, List<Integer> taskIds, DatahubApi datahubApi,
			Day2MonthSyncApi d2mSyncApi, ManApi manApi, String tmpOpTable,
			String tempFailLogTable, int onlyFlushLog) throws Exception {

		// 1. 获取最新的成功同步信息，并记录本次同步信息
		FmDay2MonSync lastSyncInfo = d2mSyncApi.queryLastedSyncInfo(region
				.getRegionId());
		log.info("获取最新的成功同步信息：" + lastSyncInfo);
		Date syncTimeStamp = new Date();

		log.info("获取大区库连接信息:" + region);
		if (region == null)
			return;

		Connection dailyConn = getConnectByRegion(region, datahubApi, "daily");
		OracleSchema dailyDbSchema = getSchemaByRegion(region, datahubApi,
				"daily");
		Connection monthConn = getConnectByRegion(region, datahubApi, "month");
		OracleSchema monthDbSchema = getSchemaByRegion(region, datahubApi,
				"month");

		LogSelector logSelector = null;
		OperationResult result = new OperationResult();
		try {
			String tempOpTable = "";
			if (StringUtils.isEmpty(tmpOpTable)
					&& StringUtils.isEmpty(tempFailLogTable)) {
				log.info("开始获取日编库履历");
				if (grids != null && grids.size() > 0) {
					logSelector = new Day2MonPoiLogByTaskIdSelector(
							dailyDbSchema, syncTimeStamp, grids, taskIds, 0);
					tempOpTable = logSelector.select();
				} else {
					logSelector = new Day2MonPoiLogByFilterGridsSelector(
							dailyDbSchema, syncTimeStamp, filterGrids, 0);
					tempOpTable = logSelector.select();
				}
				/*
				 * FlushResult flushResult = new
				 * Day2MonLogFlusher(dailyDbSchema, dailyConn, monthConn, true,
				 * tempOpTable, "day2MonSync") .flush();
				 */
				FlushResult flushResult = new Day2MonLogMultiFlusher(dailyDbSchema,
						dailyDbSchema.getPoolDataSource(),
						monthDbSchema.getPoolDataSource(), tempOpTable, true,
						"day2MonSync").flush();
				if (onlyFlushLog == 1) {
					return;
				}
				if (0 == flushResult.getTotal()) {
					log.info("没有符合条件的履历，不执行日落月，返回");
				} else {
					log.info("开始将履历搬到月库：logtotal:" + flushResult.getTotal());
					log.info("日库临时表:" + tempOpTable);
					log.info("日库失败履历临时表：" + flushResult.getTempFailLogTable());
					// 快线搬移履历是传进去的日大区库连接（刷库用的连接），如果出现异常，回滚日大区库连接即可；
					LogMover logMover = new Day2MonMover(dailyDbSchema,
							monthDbSchema, tempOpTable,
							flushResult.getTempFailLogTable());
					logMovers.add(logMover);
					LogMoveResult logMoveResult = logMover.move();
					log.info("月库临时表："
							+ logMoveResult.getLogOperationTempTable());
					log.info("开始进行履历分析");
					result = parseLog(monthConn,
							logMoveResult.getLogOperationTempTable());
					if (result.getAllObjs().size() > 0) {
						log.info("开始进行深度信息打标记");
						new DeepInfoMarker(result, monthConn).execute();
						log.info("开始执行前批");
						new PreBatch(result, monthConn).execute();
						log.info("开始执行检查");
						Map<String, Map<Long, Set<String>>> checkResult = new Check(
								result, monthConn).execute();
						new Classifier(checkResult, monthConn).execute();
						log.info("开始执行后批处理");
						new PostBatch(result, monthConn).execute();
						log.info("开始批处理MESH_ID_5K、ROAD_FLAG、PMESH_ID");
						updateField(result, monthConn);

						batchPoi(result, monthConn);
					}
					updateLogCommitStatus(dailyConn, tempOpTable);
				}
			} else {
				LogMover logMover = new Day2MonMover(dailyDbSchema,
						monthDbSchema, tmpOpTable, tempFailLogTable);
				logMovers.add(logMover);
				LogMoveResult logMoveResult = logMover.move();
				log.info("月库临时表：" + logMoveResult.getLogOperationTempTable());
				log.info("开始进行履历分析");
				result = parseLog(monthConn,
						logMoveResult.getLogOperationTempTable());
				if (result.getAllObjs().size() > 0) {
					log.info("开始进行深度信息打标记");
					new DeepInfoMarker(result, monthConn).execute();
					log.info("开始执行前批");
					new PreBatch(result, monthConn).execute();
					log.info("开始执行检查");
					Map<String, Map<Long, Set<String>>> checkResult = new Check(
							result, monthConn).execute();
					new Classifier(checkResult, monthConn).execute();
					log.info("开始执行后批处理");
					new PostBatch(result, monthConn).execute915();
					log.info("开始批处理MESH_ID_5K、ROAD_FLAG、PMESH_ID");
					updateField(result, monthConn);

					batchPoi(result, monthConn);
				}
				updateLogCommitStatus(dailyConn, tempOpTable);
			}

			// result=logFlushAndBatchData( monthDbSchema, monthConn,
			// dailyDbSchema,dailyConn,tempOpTable);
			log.info("修改同步信息为成功");
			log.info("finished:" + region.getRegionId());

		} catch (Exception e) {
			if (monthConn != null)
				monthConn.rollback();
			if (dailyConn != null)
				dailyConn.rollback();
			log.info("rollback db");

			if (logMovers != null) {
				for (LogMover l : logMovers) {
					log.info("搬移履历回滚");
					l.rollbackMove();
				}
			}
			throw e;

		} finally {
			DbUtils.commitAndCloseQuietly(monthConn);
			DbUtils.commitAndCloseQuietly(dailyConn);
			log.info("commit db");
			if (logSelector != null) {
				log.info("释放履历锁");
				logSelector.unselect(false);
			}

		}

	}

	private String createPoiTabForBatchGL(OperationResult opResult,
			OracleSchema monthDbSchema) throws Exception {
		Connection conn = monthDbSchema.getPoolDataSource().getConnection();
		int count = 0;
		try {
			// 1.粗选POI:根据operationResult解析获取要批引导link的poi数据
			if (opResult.getAllObjs().size() == 0) {
				log.info("没有获取到有变更的poi数据");
				return "";
			}

			List<Long> pids = new ArrayList<Long>();
			// 2.把精选的POI.pid放在临时表temp_poi_glink_yyyyMMddhhmmss（临时表不存在则新建）；
			String tempPoiGLinkTab = createTempPoiGLinkTable(conn);
			// 3.精选POI:根据粗选的结果，进一步过滤得到(新增POI或修改引导坐标或引导link为0的POI对象或对应引导link不存在rd_link表中)
			Set<Long> refinedPois = new HashSet<Long>();
			for (BasicObj poiObj : opResult.getAllObjs()) {
				pids.add(poiObj.objPid());
				if (OperationType.INSERT == poiObj.getMainrow().getHisOpType()
						|| (OperationType.UPDATE == poiObj.getMainrow()
								.getHisOpType() && (poiObj.getMainrow()
								.hisOldValueContains(IxPoi.Y_GUIDE)
								|| poiObj.getMainrow().hisOldValueContains(
										IxPoi.X_GUIDE) || Integer.valueOf(0)
								.equals(poiObj.getMainrow().getAttrByColName(
										"LINK_PID"))))) {
					refinedPois.add(poiObj.objPid());
				}
			}
			count = count
					+ insertPois2TempTab(refinedPois, tempPoiGLinkTab, conn);
			count = count
					+ insertPoisNotInRdLink2TempTab(
							CollectionUtils.subtract(pids, refinedPois),
							tempPoiGLinkTab, conn);
			if (count == 0) {
				return "";
			}
			return tempPoiGLinkTab;
		} catch (Exception e) {
			log.info(e.getMessage());
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	private int insertPoisNotInRdLink2TempTab(Collection<Long> pids,
			String tempPoiTable, Connection conn) throws Exception {
		String sql = "insert  into "
				+ tempPoiTable
				+ " select pid from ix_poi t  "
				+ " where t.pid in (select column_value from table(clob_to_table(?))) "
				+ " and not exists(select 1 from rd_link r where r.link_pid=t.link_pid)";
		this.log.debug("sql:" + sql);
		Clob clobPids = ConnectionUtil.createClob(conn);
		clobPids.setString(1, StringUtils.join(pids, ","));
		return new QueryRunner().update(conn, sql, clobPids);
	}

	private int insertPois2TempTab(Collection<Long> pids, String tempPoiTable,
			Connection conn) throws Exception {
		String sql = "insert into " + tempPoiTable
				+ " select column_value from table(clob_to_table(?)) ";
		this.log.debug("sql:" + sql);
		Clob clobPids = ConnectionUtil.createClob(conn);
		clobPids.setString(1, StringUtils.join(pids, ","));
		return new QueryRunner().update(conn, sql, clobPids);
	}

	private String createTempPoiGLinkTable(Connection conn) throws Exception {
		String tableName = "tmp_p_glink"
				+ (new SimpleDateFormat("yyyyMMddhhmmssS").format(new Date()));
		String sql = "create table " + tableName + " (pid number(10))";
		new QueryRunner().update(conn, sql);
		return tableName;
	}

	protected void updateField(OperationResult opResult, Connection conn)
			throws Exception {
		List<Integer> pids = new ArrayList<Integer>();
		for (BasicObj Obj : opResult.getAllObjs()) {
			IxPoi ixPoi = (IxPoi) Obj.getMainrow();
			Integer pid = (int) ixPoi.getPid();
			pids.add(pid);
		}
		if (pids == null || pids.size() <= 0) {
			return;
		}

		List<Object> values = new ArrayList<Object>();
		SqlClause inClause = SqlClause.genInClauseWithMulInt(conn, pids,
				" IP.PID  ");
		values.addAll(inClause.getValues());

		log.info("开始所有记录更新MESH_ID_5K、ROAD_FLAG");
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE IX_POI IP  SET IP.MESH_ID_5K = NAVI_GEOM.GET5KMAPNUMBER1(ip.GEOMETRY, ip.MESH_ID),IP.ROAD_FLAG  = '0'\r\n");
		sb.append(" WHERE " + inClause.getSql());
		SqlClause sqlClause = new SqlClause(sb.toString(), values);
		log.info("MESH_ID_5K、ROAD_FLAG:" + sqlClause);
		int count = sqlClause.update(conn);

		log.info("开始更新PMESH_ID");
		StringBuilder sb1 = new StringBuilder();
		sb1.append("MERGE INTO IX_POI P\r\n"
				+ " USING (SELECT IP.PID, R.MESH_ID\r\n"
				+ "          FROM IX_POI IP, RD_LINK R\r\n"
				+ "        WHERE IP.LINK_PID = R.LINK_PID\r\n"
				+ "          AND IP.PMESH_ID=0 \r\n"
				+ "          AND R.MESH_ID<>0 \r\n");
		sb1.append(" AND " + inClause.getSql());
		sb1.append(") T ON (P.PID = T.PID)\r\n" + "WHEN MATCHED THEN\r\n"
				+ "  UPDATE SET P.PMESH_ID = T.MESH_ID\r\n");
		SqlClause sqlClause1 = new SqlClause(sb1.toString(), values);
		log.info("PMESH_ID:" + sqlClause1);
		int count1 = sqlClause1.update(conn);
	}

	protected void updateLogCommitStatus(Connection dailyConn, String tempTable)
			throws Exception {
		QueryRunner run = new QueryRunner();
		String sql = "update LOG_OPERATION set com_dt = sysdate,com_sta=1,LOCK_STA=0 where OP_ID IN (SELECT OP_ID FROM "
				+ tempTable + ")";
		run.execute(dailyConn, sql);

	}

	private OperationResult parseLog(Connection monthConn, String tempOpTable)
			throws Exception, SQLException, ClassNotFoundException,
			NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, InstantiationException,
			OperationResultException {
		Map<Long, List<LogDetail>> logStatInfo = PoiLogDetailStat
				.loadByOperation(monthConn, tempOpTable);
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_ADDRESS");
		tabNames.add("IX_POI_PARKING");
		tabNames.add("IX_POI_PHOTO");
		tabNames.add("IX_POI_CARRENTAL");
		tabNames.add("IX_POI_HOTEL");
		tabNames.add("IX_POI_NAME_FLAG");
		OperationResult result = new OperationResult();
		Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(monthConn,
				"IX_POI", tabNames, false, logStatInfo.keySet(), true, true);
		ObjHisLogParser.parse(objs, logStatInfo);
		result.putAll(objs.values());
		return result;
	}

	private FmDay2MonSync createSyncInfo(Day2MonthSyncApi d2mSyncApi,
			int regionId, Date syncTimeStamp) throws Exception {
		FmDay2MonSync info = new FmDay2MonSync();
		info.setRegionId(regionId);
		info.setSyncStatus(FmDay2MonSync.SyncStatusEnum.CREATE.getValue());
		info.setJobId(this.getJobInfo().getId());
		Long sid = d2mSyncApi.insertSyncInfo(info);// 写入本次的同步信息
		info.setSid(sid);
		info.setSyncTime(syncTimeStamp);
		return info;
	}

	// 加锁的扩圈都放在DMS，DMS查和写都扩圈，所以FM查和写都不用扩圈
	private void dealFmAndDMSLock(OracleSchema monthDbSchema,
			List<Integer> meshs) throws Exception {
		Connection monthConn = monthDbSchema.getPoolDataSource()
				.getConnection();
		// 获取锁
		String sql = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE  LOCK_OWNER='GLOBAL' FOR UPDATE";
		Statement sourceStmt = monthConn.createStatement();
		try {
			ResultSet rs = sourceStmt.executeQuery(sql);
			String sqlMeshAll = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE FGM.MESH_ID = 0 ";
			ResultSet rss = sourceStmt.executeQuery(sqlMeshAll);
			int lockStatus = 0;
			while (rss.next()) {
				lockStatus = rss.getInt("LOCK_STATUS");
			}
			if (lockStatus == 1) {
				throw new Exception("DMS全库加锁");
			}
			log.info("判断是否有图幅锁");
			hasLock(monthConn, meshs);
			log.info("无锁，则图幅加锁");
			getMeshLock(monthConn, meshs);
		} catch (Exception e) {
			if (monthConn != null)
				monthConn.rollback();
			log.info("加锁图幅回滚");
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(monthConn);
		}
	}

	private void releaseMeshLock(Connection monthConn, List<Integer> meshs)
			throws Exception {
		if (meshs == null || meshs.size() == 0) {
			return;
		}
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		// 获取锁
		String sql = "SELECT FGM.LOCK_STATUS FROM FM_GEN2_MESHLOCK FGM WHERE  LOCK_OWNER='GLOBAL' FOR UPDATE";
		try {
			run.query(monthConn, sql, new ColumnListHandler("LOCK_STATUS"));// 获取锁
			sb.append("DELETE FROM FM_GEN2_MESHLOCK WHERE LOCK_STATUS=1 AND LOCK_OWNER='FM'");

			List<Object> values = new ArrayList<Object>();

			if (meshs.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(monthConn);
				clob.setString(1, StringUtils.join(meshs, ","));
				sb.append(" AND MESH_ID in (select column_value from table(clob_to_table(?))) ");
				values.add(clob);
			} else {
				sb.append(" AND MESH_ID IN (" + StringUtils.join(meshs, ",")
						+ ")");
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

		} catch (Exception e) {
			if (monthConn != null)
				monthConn.rollback();
			log.info("加锁图幅回滚");
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(monthConn);
		}

	}

	private void getMeshLock(Connection monthConn, List<Integer> meshs)
			throws Exception {
		Statement stmt = monthConn.createStatement();
		try {
			for (int m : meshs) {
				String sql = "INSERT INTO FM_GEN2_MESHLOCK (MESH_ID,LOCK_STATUS ,LOCK_OWNER,JOB_ID) VALUES ("
						+ m + ", 1,'FM','" + this.getJobInfo().getId() + "') ";
				stmt.addBatch(sql);
			}
			stmt.executeBatch();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DbUtils.close(stmt);
		}
	}

	private void hasLock(Connection monthConn, List<Integer> meshs)
			throws Exception {
		Statement sourceStmt = monthConn.createStatement();
		String sql = "SELECT FGM.MESH_ID FROM FM_GEN2_MESHLOCK FGM WHERE FGM.LOCK_OWNER='GEN2' AND FGM.LOCK_STATUS=1 ";
		ResultSet rs = sourceStmt.executeQuery(sql);
		List<Integer> gdbMeshs = new ArrayList<Integer>();
		while (rs.next()) {
			gdbMeshs.add(rs.getInt("MESH_ID"));
		}
		List<Integer> retainMeshs = new ArrayList<>(meshs);
		retainMeshs.retainAll(gdbMeshs);
		if (retainMeshs != null && retainMeshs.size() > 0) {
			throw new Exception("以下图幅DMS加锁:" + retainMeshs.toString());
		}
	}

	protected List<Integer> selectLogGridsByTaskId(List<Integer> taskIds,
			DatahubApi datahubApi, Region region, int subTaskType)
			throws Exception {

		DbInfo dailyDbInfo = datahubApi.getDbById(region.getDailyDbId());
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo
						.getConnectParam()));
		Connection conn = dailyDbSchema.getPoolDataSource().getConnection();

		try {
			QueryRunner queryRunner = new QueryRunner();
			SqlClause sqlClause = getSelectLogSql(conn, taskIds, subTaskType);

			ResultSetHandler<List<Integer>> rsh = new ResultSetHandler<List<Integer>>() {
				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					List<Integer> msgs = new ArrayList<Integer>();
					while (rs.next()) {
						msgs.add(rs.getInt("GRID_ID"));
					}
					return msgs;
				}
			};
			List<Integer> query = queryRunner.query(conn, sqlClause.getSql(),
					rsh, sqlClause.getValues().toArray());
			return query;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	protected SqlClause getSelectLogSql(Connection conn, List<Integer> grids)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" select /*+ leading(P,D,G,S)*/ distinct g.GRID_ID\r\n"
				+ "   from log_operation   p,\r\n" + "       log_detail d,\r\n"
				+ "       log_detail_grid g,\r\n"
				+ "       poi_edit_status s\r\n"
				+ "   where p.op_id = d.op_id\r\n"
				+ "    and d.row_id = g.log_row_id\r\n"
				+ "    and (d.ob_pid = s.pid or d.geo_pid = s.pid)\r\n"
				+ "    and p.com_sta = 0"
				+ "    and (d.ob_nm = 'IX_POI' or d.geo_nm = 'IX_POI')"
				+ "    and s.status = 3");

		List<Object> values = new ArrayList<Object>();
		if (grids != null && grids.size() > 0) {
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn, grids,
					" g.GRID_ID ");
			if (inClause != null) {
				sb.append(" AND " + inClause.getSql());
				values.addAll(inClause.getValues());
			}
		}
		SqlClause sqlClause = new SqlClause(sb.toString(), values);
		return sqlClause;
	}

	protected SqlClause getSelectLogSql(Connection conn, List<Integer> taskIds,
			int taskType) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" select /*+ leading(P,D,G,S)*/  distinct g.GRID_ID\r\n"
				+ "   from log_operation   p,\r\n" + "       log_detail d,\r\n"
				+ "       log_detail_grid g,\r\n"
				+ "       poi_edit_status s\r\n"
				+ "   where p.op_id = d.op_id\r\n"
				+ "    and d.row_id = g.log_row_id\r\n"
				+ "    and (d.ob_pid = s.pid or d.geo_pid = s.pid)\r\n"
				+ "    and p.com_sta = 0"
				+ "    and (d.ob_nm = 'IX_POI' or d.geo_nm = 'IX_POI')"
				+ "    and s.status = 3");
		if (taskType == 0 && (taskIds == null || taskIds.size() == 0)) {
			sb.append(" and s.medium_task_id<>0 ");
		}

		List<Object> values = new ArrayList<Object>();
		if (taskIds != null && taskIds.size() > 0) {
			String str = " s.medium_task_id ";
			if (taskType == 1) {
				str = " s.quick_task_id ";
			}
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn, taskIds,
					str);
			if (inClause != null) {
				sb.append(" AND " + inClause.getSql());
				values.addAll(inClause.getValues());
			}

		}
		SqlClause sqlClause = new SqlClause(sb.toString(), values);
		log.info("查询存在履历的grids:" + sqlClause.getSql());
		return sqlClause;
	}

	private List<Integer> grids2meshs(List<Integer> grids) throws Exception {
		List<Integer> meshs = new ArrayList<Integer>();
		for (int g : grids) {
			int mesh = (int) Math.floor(g / 100);
			if (meshs.contains(mesh)) {
				continue;
			}
			meshs.add(mesh);
		}
		return meshs;
	}

	private List<Integer> meshs2grids(List<Integer> meshs) throws Exception {
		List<Integer> grids = new ArrayList<Integer>();
		for (Object obj : meshs) {
			int m = 0;
			if (obj instanceof Integer) {
				m = (int) obj;
			} else if (obj instanceof String) {
				m = Integer.parseInt(String.valueOf(obj));
			}
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					grids.add(m * 100 + i * 10 + j);
				}
			}
		}
		return grids;
	}

	private void callDmsReleaseLockApi(long jobId) throws IOException {
		JSONObject parameter = new JSONObject();
		try {
			Map<String, String> parMap = new HashMap<String, String>();
			parameter.put("jobId", jobId);
			parMap.put("parameter", parameter.toString());
			ServiceInvokeUtil http = new ServiceInvokeUtil();
			// String msUrl
			// ="http://192.168.3.228:8086/VMWeb/springmvc/vmmanager/day2mounth/releaseLock?";
			String msUrl = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.day2mounthReleaseLock);
			String json = http.invoke(msUrl, parMap, 1000);
			log.info("调用DMS解锁接口:" + json);
		} catch (Exception e) {
			log.debug("调用DMS解锁接口:" + e.getMessage());
			// throw new IOException(e);
		}
	}

	private Map<Integer, String> getDmsLock(List<Integer> meshes, long jobId,
			DbInfo dbInfos) throws IOException {
		Map<Integer, String> dmsLockMeshes = new HashMap<Integer, String>();
		if (meshes == null || meshes.size() == 0) {
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
			JSONObject jsonReq = JSONObject
					.fromObject(callDmsGetLockApi(parameter));
			if (jsonReq.getInt("errcode") == 0) {
				if (jsonReq.get("data") != null) {
					dmsLockMeshes = (Map<Integer, String>) jsonReq.get("data");
				}
			}
			log.info("DMS被锁图幅:" + dmsLockMeshes);
		} catch (Exception e) {
			log.debug("调用DMS加锁接口:" + e.getMessage());
			throw new IOException(e);
		}
		return dmsLockMeshes;
	}

	private String callDmsGetLockApi(JSONObject parameter) throws IOException {
		String json = "";
		try {
			Map<String, String> parMap = new HashMap<String, String>();
			parMap.put("parameter", parameter.toString());
			ServiceInvokeUtil http = new ServiceInvokeUtil();
			// String msUrl
			// ="http://192.168.3.228:8086/VMWeb/springmvc/vmmanager/day2mounth/getLock?";
			String msUrl = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.day2mounthGetLock);
			json = http.invoke(msUrl, parMap, 1000);
			log.info("调用DMS加锁接口:" + json);
		} catch (Exception e) {
			log.debug("调用DMS加锁接口:" + e.getMessage());
			throw new IOException(e);
		}
		return json;
	}

	private void batchPoi(OperationResult opResult, Connection conn)
			throws Exception {

		List<Long> pids = new ArrayList<>();// 所有poiPid

		List<Long> addPids = new ArrayList<>();// 新增poiPid

		List<Long> updatePids = new ArrayList<>();// 修改poiPid

		Collection<Long> namePids = new ArrayList<>();// 改old_name

		Collection<Long> addressPids = new ArrayList<>();// 改old_address

		// 外业log:
		Collection<Long> logNamePids = new ArrayList<>();// 改名称

		Collection<Long> logAddressPids = new ArrayList<>();// 改地址

		Collection<Long> logKindCodePids = new ArrayList<>();// 改分类

		Collection<Long> logLevelPids = new ArrayList<>();// 改POI_LEVEL

		Collection<Long> logIndoorPids = new ArrayList<>();// 改内部标识

		Collection<Long> logSportPids = new ArrayList<>();// 改运动场馆

		Collection<Long> logLocationPids = new ArrayList<>();// 改RELATION

		Collection<Long> xGuidePids = new ArrayList<>();// 改xGuide

		Collection<Long> yGuidePids = new ArrayList<>();// 改yGuide

		Collection<Long> logChainPids = new ArrayList<>();// 改连锁品牌

		Collection<Long> logRatingPids = new ArrayList<>();// 改酒店星级

		Collection<Long> parkingPids = new ArrayList<>();// 停车场poi

		Collection<Long> parkingType0Pids = new ArrayList<>();// "室内"

		Collection<Long> parkingType1Pids = new ArrayList<>();// "室外"

		Collection<Long> parkingType2Pids = new ArrayList<>();// "占道"

		Collection<Long> parkingType3Pids = new ArrayList<>();// "室内地上"

		Collection<Long> parkingType4Pids = new ArrayList<>();// "地下"

		for (BasicObj obj : opResult.getAllObjs()) {

			IxPoiObj poiObj = (IxPoiObj) obj;

			long pid = poiObj.objPid();

			IxPoi poi = (IxPoi) poiObj.getMainrow();

			pids.add(pid);

			if (OperationType.UPDATE == poi.getHisOpType()) {

				updatePids.add(pid);

			} else if (OperationType.INSERT == poi.getHisOpType()) {

				addPids.add(pid);

				xGuidePids.add(pid);

				yGuidePids.add(pid);
			}

			if (poi.getKindCode() != null
					&& !poi.getKindCode().equals("230210")) {

				String label = poi.getLabel();

				if (label != null
						&& (label.contains("室内|") || label.contains("室外|")
								|| label.contains("占道|")
								|| label.contains("室内地上|") || label
									.contains("地下|"))) {
					parkingPids.add(pid);
				}

				if (poiObj.getIxPoiParkings() != null
						&& poiObj.getIxPoiParkings().size() == 1) {

					String parkingType = poiObj.getIxPoiParkings().get(0)
							.getParkingType();

					if (parkingType.contains("0")) {
						parkingType0Pids.add(pid);
					}
					if (parkingType.contains("1")) {
						parkingType1Pids.add(pid);
					}
					if (parkingType.contains("2")) {
						parkingType2Pids.add(pid);
					}
					if (parkingType.contains("3")) {
						parkingType3Pids.add(pid);
					}
					if (parkingType.contains("4")) {
						parkingType4Pids.add(pid);
					}
				}
			}

			if (poi.getHisOpType() == OperationType.UPDATE) {

				if (poi.hisOldValueContains(IxPoi.KIND_CODE)) {
					logKindCodePids.add(pid);
				}
				if (poi.hisOldValueContains(IxPoi.LEVEL)) {
					logLevelPids.add(pid);
				}
				if (poi.hisOldValueContains(IxPoi.INDOOR)) {
					logIndoorPids.add(pid);
				}
				if (poi.hisOldValueContains(IxPoi.SPORTS_VENUE)) {
					logSportPids.add(pid);
				}
				if (poi.hisOldValueContains(IxPoi.GEOMETRY)) {
					logLocationPids.add(pid);
				}
				if (poi.hisOldValueContains(IxPoi.X_GUIDE)) {
					xGuidePids.add(pid);
				}
				if (poi.hisOldValueContains(IxPoi.Y_GUIDE)) {
					yGuidePids.add(pid);
				}
				if (poi.hisOldValueContains(IxPoi.CHAIN)) {
					logChainPids.add(pid);
				}
			}
			// 作业季新增修改中文地址
			if (poiObj.getChiAddress() != null) {
				IxPoiAddress address = poiObj.getChiAddress();
				if (address.getHisOpType() == OperationType.UPDATE) {
					logAddressPids.add(pid);
				}
				if (poi.getOldAddress() == null
						|| !poi.getOldAddress().equals(
								poiObj.getChiAddress().getFullname())) {
					addressPids.add(pid);
				}

			}
			// 作业季新增修改中文原始
			if (poiObj.getOfficeOriginCHName() != null) {
				IxPoiName poiName = poiObj.getOfficeOriginCHName();
				if (poiName.getHisOpType() == OperationType.UPDATE) {
					logNamePids.add(pid);
				}
				if (poi.getOldName() == null
						|| !poi.getOldName().equals(
								poiObj.getOfficeOriginCHName().getName())) {
					namePids.add(pid);
				}
			}
			// 作业季修改酒店星级
			if (poiObj.getIxPoiHotels() != null
					&& poi.getHisOpType() == OperationType.UPDATE) {

				for (IxPoiHotel hotel : poiObj.getIxPoiHotels()) {
					if (hotel.getHisOpType() == OperationType.UPDATE
							&& hotel.hisOldValueContains(IxPoiHotel.RATING)) {
						logRatingPids.add(pid);
						break;
					}
				}
			}
		}

		log.info("批记录状态state");
		this.updateBatchPoi(addPids, this.getStateParaSql(3), conn);
		this.updateBatchPoi(updatePids, this.getStateParaSql(2), conn);

		log.info("批old_name");
		this.updateBatchPoi(namePids, this.getUpdatePoiOldNameSql(), conn);

		log.info("批old_address");
		this.updateBatchPoi(addressPids, this.getUpdatePoiOldAddressSql(), conn);

		log.info("批old_kind");
		this.updateBatchPoi(pids, this.getUpdatePoiOldKindCodeSql(), conn);

		log.info("批外业log");
		this.updateBatchPoi(logNamePids, this.getUpadeLogForSql("改名称"), conn);
		this.updateBatchPoi(logAddressPids, this.getUpadeLogForSql("改地址"), conn);
		this.updateBatchPoi(logKindCodePids, this.getUpadeLogForSql("改分类"),
				conn);
		this.updateBatchPoi(logLevelPids, this.getUpadeLogForSql("改POI_LEVEL"),
				conn);
		this.updateBatchPoi(logSportPids, this.getUpadeLogForSql("改运动场馆"), conn);
		this.updateBatchPoi(logIndoorPids, this.getUpadeLogForSql("改内部标识"),
				conn);
		this.updateBatchPoi(logLocationPids,
				this.getUpadeLogForSql("改RELATION"), conn);

		log.info("批验证标识");
		Collection<Long> metaPids = this.getMetaPidsForPoi(conn);
		metaPids.retainAll(pids);
		pids.removeAll(metaPids);
		this.updateBatchPoi(metaPids, this.getVerifiedParaSql(3), conn);
		this.updateBatchPoi(pids, this.getVerifiedParaSql(9), conn);

		log.info("批几何调整标识 精编标识  数据采集版本");
		this.updateBatchPoi(pids, this.getBatchPoiCommonSql(), conn);

		log.info("批Old_X_Guide");
		this.updateBatchPoi(xGuidePids, this.getUpdatePoiOldXGuideSql(), conn);

		log.info("批Old_Y_Guide");
		this.updateBatchPoi(yGuidePids, this.getUpdatePoiOldYGuideSql(), conn);

		log.info("批 FieldState");
		this.updateBatchPoi(logKindCodePids,
				this.getUpadeFieldStateForSql("改种别代码"), conn);
		this.updateBatchPoi(logChainPids,
				this.getUpadeFieldStateForSql("改连锁品牌"), conn);
		this.updateBatchPoi(logRatingPids,
				this.getUpadeFieldStateForSql("改酒店星级"), conn);

		log.info("批处理标记");
		this.updateBatchPoi(parkingPids, this.getDelLabelForSql(), conn);
		this.updateBatchPoi(parkingType0Pids, this.getUpadeLabelForSql("室内|"),
				conn);
		this.updateBatchPoi(parkingType1Pids, this.getUpadeLabelForSql("室外|"),
				conn);
		this.updateBatchPoi(parkingType2Pids, this.getUpadeLabelForSql("占道|"),
				conn);
		this.updateBatchPoi(parkingType3Pids,
				this.getUpadeLabelForSql("室内地上|"), conn);
		this.updateBatchPoi(parkingType4Pids, this.getUpadeLabelForSql("地下|"),
				conn);

		log.info("外业任务编号");
		this.updateBatchPoi(pids, this.getFieldTaskIdSql(), conn);
	}

	private String getStateParaSql(int state) {
		return "update ix_poi p set state = "
				+ state
				+ "  where  pid in (select to_number(column_value) from table(clob_to_table(?)))";
	}

	private String getUpdatePoiOldNameSql() {
		return "   UPDATE ix_poi p								                           \n"
				+ "    SET p.old_name =                                                    \n"
				+ "          (SELECT n.name                                               \n"
				+ "                FROM ix_poi_name n                                      \n"
				+ "               WHERE n.name_class = 1                                    \n"
				+ "                     AND n.lang_code IN ('CHI', 'CHT')                   \n"
				+ "                     AND n.name_type = 2                                 \n"
				+ "                     AND n.poi_pid = p.pid                               \n"
				+ "                     AND NVL (p.old_name, -1) <> NVL (n.name, -1)      \n"
				+ "                     AND rownum =1)                                        \n"
				+ "     WHERE p.pid in  (select to_number(column_value) from table(clob_to_table(?)))  \n";
	}

	private String getUpdatePoiOldAddressSql() {
		return "   UPDATE ix_poi p								                                       \n"
				+ "    SET p.old_address =                                                             \n"
				+ "          (SELECT n.fullname                                                        \n"
				+ "                FROM ix_poi_address n                                               \n"
				+ "               WHERE n.lang_code IN ('CHI', 'CHT')                                  \n"
				+ "                     AND n.poi_pid = p.pid                                          \n"
				+ "                     AND NVL (p.old_address, -1) <> NVL (n.fullname, -1)          \n"
				+ "                     AND rownum =1)                                                \n"
				+ "     WHERE p.pid in  (select to_number(column_value) from table(clob_to_table(?)))   \n";
	}

	private String getUpdatePoiOldKindCodeSql() {
		return "   UPDATE ix_poi p								                                       \n"
				+ "    SET p.old_kind = p.kind_code                                                    \n"
				+ "     WHERE p.pid in  (select to_number(column_value) from table(clob_to_table(?)))    \n"
				+ "           AND NVL (p.old_kind, -1) <> NVL (p.kind_code, -1)                        \n";
	}

	private String getUpadeLogForSql(String logName) {

		return "   UPDATE ix_poi p								               \n"
				+ "    SET    p.log =  DECODE (INSTR (p.LOG, '"
				+ logName
				+ "'),                                                          \n"
				+ "                          NULL,  '"
				+ logName
				+ "|',                                                          \n"
				+ "                           0, p.LOG ||  '"
				+ logName
				+ "|',                         \n"
				+ "                           p.LOG)                                        \n"
				+ "     WHERE p.pid in  (select to_number(column_value) from table(clob_to_table(?))) \n";
	}

	/**
	 * 获取所有的代理店的poi的pid列表 【判断POI是否为代理店POI的方式】：
	 * 根据配置表SC_POINT_SPEC_KINDCODE_NEW与POI的种别，CHAIN等信息匹配，确定是否是代理店：：
	 * 1）配置表中TYPE=7且Category=1的记录的POI_KIND与POI的kindcode匹配，则说明是代理店；
	 * 2）配置表中TYPE=7且Category
	 * =3的记录的POI_KIND、CHAIN与POI的kindcode、brands.code匹配，则说明是代理店；
	 * 3）配置表中TYPE=7且Category
	 * =7的记录的POI_KIND、CHAIN、HM_FLAG与POI的kindcode、brands.code
	 * 、“大陆数据还是港澳数据”匹配，则说明是代理店； 以上均不满足，则说明是非代理店。 补充说明：poi是港澳的 则匹配HM或DHM；poi是大陆的
	 * 则匹配D或DHM
	 * 
	 */
	private Collection<Long> getMetaPidsForPoi(Connection conn)
			throws Exception {

		// 得到所有“表内代理店分类的POI”的PiD
		// 区分大陆港澳
		String hkFlagStr = "'D','DHM'";

		String tmpMetaTableCreateSql = "select * from(                             \n"
				+ "select p.pid                                                             \n"
				+ "  from sc_point_spec_kindCode_new@DBLINK_RMS M, ix_poi p     \n"
				+ " where m.type = 7                                                        \n"
				+ "   and m.category = 1                                                    \n"
				+ "   and p.kind_code = m.poi_kind                                          \n"
				+ "union                                                                    \n"
				+ "select p.pid                                                             \n"
				+ "  from sc_point_spec_kindCode_new@DBLINK_RMS M, ix_poi p     \n"
				+ " where m.type = 7                                                        \n"
				+ "   and m.category = 3                                                    \n"
				+ "   and p.kind_code = m.poi_kind                                          \n"
				+ "   and p.chain = m.chain                                                 \n"
				+ "union                                                                    \n"
				+ "select p.pid                                                             \n"
				+ "  from sc_point_spec_kindCode_new@DBLINK_RMS M, ix_poi p     \n"
				+ " where m.type = 7                                                        \n"
				+ "   and m.category = 7                                                    \n"
				+ "   and p.kind_code = m.poi_kind                                          \n"
				+ "   and p.chain = m.chain                                                 \n"
				+ "   and m.hm_flag in ("
				+ hkFlagStr
				+ "))                                                                        \n";
		return new QueryRunner().query(conn, tmpMetaTableCreateSql,
				new Day2MonthPoiMerge915TmpJob.PidHandler());

	}

	class PidHandler implements ResultSetHandler<Collection<Long>> {
		@Override
		public Collection<Long> handle(ResultSet rs) throws SQLException {

			Collection<Long> resultPids = new ArrayList<>();
			while (rs.next()) {
				resultPids.add(rs.getLong("pid"));
			}
			return resultPids;
		}

	}

	private String getVerifiedParaSql(int verifiedFlag) {
		return "update ix_poi p set verified_flag = "
				+ verifiedFlag
				+ "  where  pid in (select to_number(column_value) from table(clob_to_table(?)))";
	}

	/**
	 * 几何调整标识 精编标识 数据采集版本
	 */
	private String getBatchPoiCommonSql() {
		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.seasonVersion);
		return " UPDATE ix_poi p    \n"
				+ "   SET p.geo_adjust_flag = 1 ,\n"
				+ "       p.full_attr_flag = 1 ,  \n"
				+ "       p.data_version = '"
				+ gdbVersion
				+ "'   \n"
				+ "     WHERE p.pid in (select to_number(column_value) from table(clob_to_table(?))) \n";
	}

	private String getUpdatePoiOldXGuideSql() {
		return "   update ix_poi p								                                       \n"
				+ "    set p.old_x_guide = p.x_guide                                                    \n"
				+ "     where p.pid in  (select to_number(column_value) from table(clob_to_table(?)))    \n"
				+ "           and nvl (p.old_x_guide, -1) <> nvl (p.x_guide, -1)                        \n";
	}

	private String getUpdatePoiOldYGuideSql() {
		return "   update ix_poi p								                                       \n"
				+ "    set p.old_y_guide = p.y_guide                                                    \n"
				+ "     where p.pid in  (select to_number(column_value) from table(clob_to_table(?)))    \n"
				+ "           and nvl (p.old_y_guide, -1) <> nvl (p.y_guide, -1)                        \n";
	}

	private String getUpadeFieldStateForSql(String strValue) {

		return "   UPDATE ix_poi p								               \n"
				+ "    SET    p.FIELD_STATE =  DECODE (INSTR (p.FIELD_STATE, '"
				+ strValue
				+ "'),                                                          \n"
				+ "                          NULL,  '"
				+ strValue
				+ "|',                                                          \n"
				+ "                           0, p.FIELD_STATE ||  '"
				+ strValue
				+ "|',                         \n"
				+ "                           p.FIELD_STATE)                                        \n"
				+ "     WHERE p.pid in  (select to_number(column_value) from table(clob_to_table(?))) \n";
	}

	private String getFieldTaskIdSql() {
		return "MERGE INTO IX_POI P\n"
				+ "USING (SELECT T2.OB_PID, MAX(A.STK_ID) STK_ID\n"
				+ "         FROM LOG_OPERATION O2,\n"
				+ "              LOG_ACTION A,\n"
				+ "              (SELECT D1.OB_PID, MAX(D1.OP_ID) OP_ID\n"
				+ "                 FROM LOG_DETAIL D1,\n"
				+ "                      LOG_OPERATION O1,\n"
				+ "                      (SELECT D.OB_PID, MAX(O.OP_DT) MAX_DT\n"
				+ "                         FROM LOG_DETAIL D, LOG_OPERATION O\n"
				+ "                        WHERE D.OB_NM = 'IX_POI'\n"
				+ "                          AND D.OP_ID = O.OP_ID\n"
				+ "                          AND D.OB_PID IN\n"
				+ "                              (SELECT TO_NUMBER(COLUMN_VALUE)\n"
				+ "                                 FROM TABLE(CLOB_TO_TABLE(?)))\n"
				+ "                        GROUP BY D.OB_PID) T\n"
				+ "                WHERE D1.OP_ID = O1.OP_ID\n"
				+ "                  AND T.MAX_DT = O1.OP_DT\n"
				+ "                  AND T.OB_PID = D1.OB_PID\n"
				+ "                GROUP BY D1.OB_PID) T2\n"
				+ "        WHERE A.ACT_ID = O2.ACT_ID\n"
				+ "          AND O2.OP_ID = T2.OP_ID\n"
				+ "        GROUP BY T2.OB_PID) C\n" + "ON (P.PID = C.OB_PID)\n"
				+ "WHEN MATCHED THEN\n"
				+ "  UPDATE SET P.FIELD_TASK_ID = C.STK_ID";
	}

	private String getDelLabelForSql() {

		return " UPDATE IX_POI P\n"
				+ "   SET P.LABEL = REGEXP_REPLACE(P.LABEL,'室内\\||室外\\||占道\\||室内地上\\||地下\\|',\n"
				+ "                                   '')"
				+ "     WHERE P.PID IN  (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?))) \n";
	}

	private String getUpadeLabelForSql(String strValue) {

		return " UPDATE ix_poi p								               \n"
				+ "    SET    p.LABEL =  DECODE (INSTR (p.LABEL, '"
				+ strValue
				+ "'),                                                          \n"
				+ "                          NULL,  '"
				+ strValue
				+ "|',                                                          \n"
				+ "                           0, p.LABEL ||  '"
				+ strValue
				+ "|',                         \n"
				+ "                           p.LABEL)                                        \n"
				+ "     WHERE p.pid in  (select to_number(column_value) from table(clob_to_table(?))) \n";
	}

	private void updateBatchPoi(Collection<Long> pidList, String sql,
			Connection conn) throws Exception {

		if (pidList == null || pidList.size() == 0) {
			return;
		}
		PreparedStatement pstmt = null;

		try {
			Clob pidsClob = ConnectionUtil.createClob(conn);
			pidsClob.setString(1, StringUtils.join(pidList, ","));
			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, pidsClob);
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}

	public static void main(String[] args) throws JobException {
		new Day2MonthPoiMergeJob(null).execute();
	}

}
