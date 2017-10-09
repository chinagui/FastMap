package com.navinfo.dataservice.row.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/**
 * @Title: PointAddressRowValidationJob
 * @Package: com.navinfo.dataservice.row.job
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月27日
 * @Version: V1.0
 */
public class PointAddressRowValidationJob extends AbstractJob {

	public PointAddressRowValidationJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		Date startTime = new Date();
		log.info("start time:" + startTime);
		log.info("start PointAddressRowValidationJob");
		PointAddressRowValidationJobRequest myRequest = (PointAddressRowValidationJobRequest) request;
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getConnectionById(myRequest.getTargetDbId());
			log.info("PointAddressRowValidationJob:获取要检查的数据pid");
			// 获取要检查的数据pid
			getCheckPidList(conn, myRequest);

			log.info("PointAddressRowValidationJob:需要检查的数据共计：" + myRequest.getPids().size());
			log.info("PointAddressRowValidationJob:获取要检查的数据的履历");
			// 获取log
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadPointAddressByRowEditStatus(conn, myRequest.getPids());
			Set<String> tabNames = getChangeTableSet(logs);
			log.info("PointAddressRowValidationJob:加载检查对象");
			// 获取poi对象
			Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POINTADDRESS, tabNames, false,
					myRequest.getPids(), false, false);
			log.info("PointAddressRowValidationJob:本次检查POI数量：" + objs.size());
			// 将poi对象与履历合并起来
			ObjHisLogParser.parse(objs, logs);
			log.info("PointAddressRowValidationJob:执行检查");
			
			// 构造检查参数，执行检查
			OperationResult operationResult = new OperationResult();
			Map<String, Map<Long, BasicObj>> objsMap = new HashMap<String, Map<Long, BasicObj>>();
			objsMap.put(ObjectName.IX_POINTADDRESS, objs);
			operationResult.putAll(objsMap);

			CheckCommand checkCommand = new CheckCommand();

			if (myRequest.getRules() != null && myRequest.getRules().size() > 0) {
				checkCommand.setRuleIdList(myRequest.getRules());
			} else {
				checkCommand.setOperationName(getOperationName());
			}

			// 清理检查结果
			log.info("start 清理检查结果");
			DeepCoreControl deepControl = new DeepCoreControl();
			List<Integer> pidIntList = new ArrayList<Integer>();
			for (Long pidTmp : myRequest.getPids()) {
				pidIntList.add(Integer.valueOf(pidTmp.toString()));
			}
			deepControl.cleanExByCkRule(myRequest.getTargetDbId(), pidIntList, checkCommand.getRuleIdList(), ObjectName.IX_POINTADDRESS);
			log.info("end 清理检查结果");

			Check check = new Check(conn, operationResult);
			check.operate(checkCommand);

			// 查询检查结果数量
			int resultCount = 0;
			resultCount = getListPoiResultCount(conn, myRequest);
			JSONObject data = new JSONObject();
			data.put("type", "检查");
			data.put("resNum", resultCount);
			this.exeResultMsg = " #" + data.toString() + "#";
			log.info("查询poi检查结果数量:" + resultCount);
			log.info("end PointAddressRowValidationJob");
			Date endTime = new Date();
			log.info("end time:" + endTime);
			log.info("本次检查共计耗时：" + (endTime.getTime() - startTime.getTime()) / 1000 + "s");
		} catch (Exception e) {
			log.error("PointAddressRowValidationJob错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	/**
	 * 获取行编检查对象pid
	 * 1.pids有值，则直接针对改pid进行检查
	 * 2.pids无值,根据子任务圈查询，待作业/已作业状态的非删除点门牌列表
	 * @param conn
	 * @param myRequest
	 * @throws JobException
	 */
	private void getCheckPidList(Connection conn, PointAddressRowValidationJobRequest myRequest) throws JobException {
		try {
			List<Long> pids = myRequest.getPids();
			if (pids != null && pids.size() > 0) {
				return;
			}
			// 行编有针对删除数据进行的检查，此处要把删除数据也加载出来
			String sql = " SELECT IP.PID FROM IX_POINTADDRESS IP, POINTADDRESS_EDIT_STATUS DS WHERE IP.PID = DS.PID AND DS.WORK_TYPE = 1 AND DS.STATUS IN (1, 2) "
					+ " AND ( DS.QUICK_SUBTASK_ID = " + (int) jobInfo.getTaskId() + " OR DS.MEDIUM_SUBTASK_ID = "
					+ (int) jobInfo.getTaskId() + " ) ";
			QueryRunner run = new QueryRunner();
			pids = run.query(conn, sql, new ResultSetHandler<List<Long>>() {

				@Override
				public List<Long> handle(ResultSet rs) throws SQLException {
					List<Long> pids = new ArrayList<Long>();
					while (rs.next()) {
						pids.add(rs.getLong("PID"));
					}
					return pids;
				}
			});
			myRequest.setPids(pids);
		} catch (Exception e) {
			log.error("点门牌行编获取检查数据报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}
	}

	/**
	 * 分析履历，将履历中涉及的变更过的子表集合返回
	 * @param logs
	 * @return
	 */
	private Set<String> getChangeTableSet(Map<Long, List<LogDetail>> logs) {
		Set<String> subtables = new HashSet<String>();
		if (logs == null || logs.size() == 0) {
			return subtables;
		}
		String mainTable = "IX_POINTADDRESS";
		for (List<LogDetail> logList : logs.values()) {
			for (LogDetail logTmp : logList) {
				String tableName = logTmp.getTbNm();
				if (!mainTable.equals(tableName)) {
					subtables.add(tableName);
				}
			}
		}
		return subtables;
	}

	
	public String getOperationName() {
		return "POINTADDRESS_ROW_COMMIT";
	}
	
	/**
	 * 获取去子任务范围内所有poi 检查结果的总条数
	 * @param conn
	 * @param myRequest
	 * @return
	 * @throws Exception
	 */
	private int getListPoiResultCount(Connection conn,PointAddressRowValidationJobRequest myRequest)
			throws Exception {

		List<Long> pids = myRequest.getPids();
		int poiResCount = 0;
		if (pids != null && pids.size() > 0) {
			try {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				StringBuilder sql = new StringBuilder(
						"SELECT COUNT(1) TOTAL FROM ( "
								+ "SELECT O.PID "
								+ "FROM "
								+ "NI_VAL_EXCEPTION A  , CK_RESULT_OBJECT O  "
								+ "WHERE  (O.TABLE_NAME LIKE 'IX_POINTADDRESS\\_%' ESCAPE '\\' OR O.TABLE_NAME ='IX_POINTADDRESS')  AND O.MD5_CODE = A.MD5_CODE "
								+ " AND O.PID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)) "
								+ ") "
								+ " UNION ALL "
								+ "SELECT O.PID "
								+ "FROM "
								+ "CK_EXCEPTION C , CK_RESULT_OBJECT O "
								+ "  WHERE (O.TABLE_NAME LIKE 'IX_POINTADDRESS\\_%' ESCAPE '\\' OR O.TABLE_NAME ='IX_POINTADDRESS')  AND O.MD5_CODE = C.MD5_CODE "
								+ " AND O.PID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)) "
								+ " )  " + " )  B ");
				
				log.info("SQL: " +sql.toString());
				QueryRunner run = new QueryRunner();
				poiResCount = run.query(conn, sql.toString(),
						new ResultSetHandler<Integer>() {

							@Override
							public Integer handle(ResultSet rs)
									throws SQLException {
								Integer resCount = 0;
								if (rs.next()) {
									resCount = rs.getInt("TOTAL");
								}
								return resCount;
							}
						},clob,clob);

			} catch (Exception e) {
				log.error("点门牌行编获取检查数据报错", e);
				throw new Exception(e);
			}
		}
		log.info("poiResCount: " + poiResCount);
		return poiResCount;
	}
}
