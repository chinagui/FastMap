package com.navinfo.dataservice.engine.fcc.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.fcc.TipsWorkStatus;
import com.navinfo.dataservice.dao.fcc.check.model.CheckTask;
import com.navinfo.dataservice.dao.fcc.check.operate.CheckTaskOperator;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckPercentConfig;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;
import com.navinfo.dataservice.engine.fcc.tips.solrquery.TipsRequestParam;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.sun.tools.internal.ws.processor.model.java.JavaArrayType;

/**
 * @ClassName: TipsExtract.java
 * @author y
 * @date 2017-5-26 上午10:58:58
 * @Description: 质检-tips抽检
 * 
 */
public class TipsExtract {

	private SolrController solrConn = new SolrController();

	private static final Logger logger = Logger.getLogger(SolrController.class);

	/**
	 * @Description:tips抽取：抽取当前质检任务所对应的作业任务的，已作业完成、handler=作业员、作业子任务范围内（grid+task过滤）
	 * @param checkTaskId
	 * @param checkerId
	 * @param checkerName
	 * @param grids
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-5-26 上午11:02:48
	 */
	public JSONObject doExtract(int checkTaskId, int checkerId,
			String checkerName, JSONArray grids) throws Exception {

		JSONObject result = new JSONObject();

		int total = 0;

		// 1.先查询范围内的tips
		int workStatus = TipsWorkStatus.PREPARED_CHECKING; // 待质检

		try {

			// 调用 manapi 获取 任务类型、及任务号
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");

			Map<String, String> taskInfoMap = manApi
					.getCommonSubtaskByQualitySubtask(checkTaskId);
			Integer workTaskId = Integer.valueOf(taskInfoMap.get("subtaskId"));// 作业任务号
			// 1.判断作业任务是否关闭

			Subtask subTask = manApi.queryBySubtaskId(workTaskId);
			int subTaskStatus = subTask.getStatus();// 0:关闭 ；1开启

			if (subTaskStatus != 0) {
				throw new IllegalArgumentException("日编子任务未关闭，不能进行抽取质检。");
			}

			/**
			 * 通过质检子任务id获取常规子任务相关信息。用于编辑过程中tips质检子任务
			 * 
			 * @param qualitySubtaskId
			 * @return Map<String, String> returnMap=new HashMap<String,
			 *         String>(); returnMap.put("subtaskId",
			 *         rs.getString("SUBTASK_ID")); returnMap.put("exeUserId",
			 *         rs.getString("EXE_USER_ID"));
			 *         returnMap.put("exeUserName",
			 *         rs.getString("USER_REAL_NAME")); returnMap.put("groupId",
			 *         rs.getString("GROUP_ID")); returnMap.put("groupName",
			 *         rs.getString("GROUP_NAME"));
			 *         returnMap.put("finishedRoad",
			 *         rs.getString("FINISHED_ROAD"));
			 *         returnMap.put("subtaskName",
			 *         rs.getString("SUBTASK_NAME")); returnMap.put("taskName",
			 *         rs.getString("TASK_NAME"));
			 * @throws Exception
			 */

			Integer workerId = Integer.valueOf(taskInfoMap.get("exeUserId"));// 作业员编号
			String workerName = taskInfoMap.get("exeUserName");// 作业员姓名
			String groupName = taskInfoMap.get("groupName");// 组名
			int workTotalCount = 0;
			if (taskInfoMap.get("finishedRoad") != null) {
				workTotalCount = Integer.valueOf(taskInfoMap
						.get("finishedRoad"));// 作业量
			}
			String subTaskName = taskInfoMap.get("subtaskName");// 子任务名称
			String taskName = taskInfoMap.get("taskName");// 任务名称

			if (grids == null || grids.size() == 0) {
				throw new IllegalArgumentException("参数错误:grids不能为空。");
			}
			// 没有数据，异常抛出
			Map<String, Integer> finishedMap = queryhasWorkTipsCount(grids,
					workStatus, checkTaskId, workTaskId, workerId, 0, null);

			// 2.查询抽取配置表.计算出每类tips需要抽取的数量.并进行tips抽取
			Map<String, Integer> extactCountMap = new HashMap<String, Integer>();// 每个tips抽取数量映射

			CheckPercentConfig configClass = new CheckPercentConfig();

			Map<String, Integer> percentConfig = configClass.getConfig();

			Set<String> allType = finishedMap.keySet();

			// 抽检后tips类型数（应为存在抽检比例没有配置的，默认0）

			Set<String> extType = new HashSet<String>();

			for (String type : allType) {

				int typeAllCount = finishedMap.get(type);

				int exPercent = 0;

				if (percentConfig.get(type) != null) {
					exPercent = percentConfig.get(type);
				}

				Double exCout = Math.ceil((double) typeAllCount * exPercent
						/ 100);

				int count = exCout.intValue();

				if (count > 0) {

					extactCountMap.put(type, exCout.intValue());

					// 抽检条数不为0才进行统计

					extType.add(type);
				}

			}

			// 3.进行tips抽取
			List<TipsDao> allExpTipsList = new ArrayList<TipsDao>();
			java.sql.Connection conn = null;

			List<TipsDao> tipsDaos = null;

			// 1.wkt过滤
			String wkt = GridUtils.grids2Wkt(grids);
			try {
				conn = DBConnector.getInstance().getTipsIdxConnection();
				for (String type : extType) {

					int extactLimit = extactCountMap.get(type);

					TipsRequestParam param = new TipsRequestParam();

					String solrQuery = param.getQueryFilterSqlForCheck(
							workStatus, workTaskId, workerId, 0, null);

					solrQuery = solrQuery + " AND s_sourceType='" + type+"'"; // 指定类型

					// String solrQuery =
					// param.getQueryFilterSqlForCheck(workStatus,subTaskId,workerId,checkerId,rowkeyList);

					logger.debug("tips extract query sql:" + solrQuery);

					// List<JSONObject> tips = solrConn.queryTips(solrQuery,
					// null);

					TipsIndexOracleOperator tipsIndexOracleOperator = new TipsIndexOracleOperator(
							conn);
					tipsDaos = tipsIndexOracleOperator.queryWithLimit(
							solrQuery, extactLimit, ConnectionUtil.createClob(conn, wkt));

					// List<JSONObject> tips = solrConn.queryTips(solrQuery,
					// null,extactLimit);

					allExpTipsList.addAll(tipsDaos);
				}

			} catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				logger.error("queryTipsByWorkState error", e);
				throw e;
			} finally {
				DbUtils.commitAndCloseQuietly(conn);
			}

			// 4.更新tips的状态，更新 stage=7,t_dEditStatus=0,t_dEditMeth=0

			updateTipsStatus2Check(allExpTipsList, checkerId);

			// 4.保存tips抽取的tips结果
			/*
			 * CheckResultOperator operate=new CheckResultOperator();
			 * 
			 * operate.save(checkTaskId, total, allExpTipsList);
			 */

			total = allExpTipsList.size();

			CheckTask task = new CheckTask();

			task.setTaskId(checkTaskId);

			task.setTaskName(taskName);

			task.setSubTaskName(subTaskName);

			task.setWokerInfo(workerName + workerId);

			task.setCheckInfo(checkerName + checkerId);

			task.setWorkGroup(groupName);

			task.setWorkTotalCount(workTotalCount);

			task.setCheckTotalCount(total);

			task.setCheckStatus(0); // 待质检

			task.setTipTypeCount(extType.size());

			CheckTaskOperator taskOperate = new CheckTaskOperator();

			taskOperate.save(task);

			result.put("total", total);

			result.put("typeCount", extType.size());

			return result;

		} catch (Exception e) {

			logger.error("tips抽取出错：" + e.getMessage(), e);

			throw new Exception("tips抽取出错：" + e.getMessage(), e);
		}
	}

	/**
	 * @Description:更新抽检后的tips的状态 ：更新 stage=7,t_dEditStatus=0,t_dEditMeth=0
	 * @param allExpTipsList
	 * @author: y
	 * @time:2017-5-27 下午6:02:23
	 */
	private void updateTipsStatus2Check(List<TipsDao> allExpTipsList,
			int checkerId) throws Exception {

		Connection hbaseConn = null;
		java.sql.Connection conn = null;
		Table htab = null;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			int t_dEditStatus = 0;

			int t_dEditMeth = 0;

			int stage = 7;

			String date = StringUtils.getCurrentTime();
			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator tipsIndexOracleOperator = new TipsIndexOracleOperator(
					conn);
			List<Put> puts = new ArrayList<Put>();
			for (TipsDao tipsDao : allExpTipsList) {

				String rowkey = tipsDao.getId();

				// 获取solr数据
				// JSONObject solrIndex = solrConn.getById(rowkey);
				tipsDao.setT_dEditStatus(t_dEditStatus);
				tipsDao.setT_dEditMeth(t_dEditMeth);
				tipsDao.setT_date(date);
				tipsDao.setStage(stage);
				tipsDao.setHandler(checkerId);

				String[] queryColNames = { "track" };

				JSONObject oldTip = HbaseTipsQuery.getHbaseTipsByRowkey(htab,
						rowkey, queryColNames);

				JSONObject track = oldTip.getJSONObject("track");

				JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

				// 更新hbase
				JSONObject newTrackInfo = TipsUtils.newTrackInfo(stage, date,
						checkerId);
				trackInfoArr.add(newTrackInfo);

				track.put("t_dEditStatus", t_dEditStatus);
				track.put("t_dEditMeth", t_dEditMeth);
				track.put("t_trackInfo", trackInfoArr);

				Put put = new Put(rowkey.getBytes());

				put.addColumn("data".getBytes(), "track".getBytes(), track
						.toString().getBytes());

				puts.add(put);

			}
			htab.put(puts);
			tipsIndexOracleOperator.update(allExpTipsList);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error("更细质检状态出错：" + e.getMessage(), e);

			throw new Exception("更新质检状态出错：" + e.getMessage(), e);
		}finally {
            DbUtils.commitAndCloseQuietly(conn);
			if(htab != null) {
                htab.close();
            }
		}

	}

	/**
	 * 
	 * @Description：按照tips作业昨天查询tips
	 * @param grids
	 * @param workStatus
	 * @param checkTaskId
	 * @param subTaskId
	 * @param workerId
	 * @param checkerId
	 * @param rowkeyList
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-5-26 下午1:45:09
	 */
	private List<TipsDao> queryTipsByWorkState(JSONArray grids, int workStatus,
			int checkTaskId, int subTaskId, int workerId, int checkerId,
			JSONArray rowkeyList) throws Exception {
		TipsRequestParam param = new TipsRequestParam();
		// 1.wkt过滤
		String wkt = GridUtils.grids2Wkt(grids);
		String solrQuery = param.getQueryFilterSqlForCheck(workStatus,
				subTaskId, workerId, checkerId, rowkeyList);

		logger.debug("tips extract query sql:" + solrQuery);

		java.sql.Connection conn = null;
		List<TipsDao> tipsDaos = null;
		// List<JSONObject> tips = solrConn.queryTips(solrQuery, null);
		try {
			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator tipsIndexOracleOperator = new TipsIndexOracleOperator(
					conn);
			tipsDaos = tipsIndexOracleOperator.query(solrQuery, ConnectionUtil.createClob(conn, wkt));
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error("queryTipsByWorkState error", e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

		return tipsDaos;

	}

	/**
	 * @Description:获取到每种tips的个数
	 * @param grids
	 * @param workStatus
	 * @param checkTaskId
	 * @param subTaskId
	 * @param workerId
	 * @param checkerId
	 * @param rowkeyList
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-5-26 下午1:50:35
	 */
	private Map<String, Integer> queryhasWorkTipsCount(JSONArray grids,
			int workStatus, int checkTaskId, int subTaskId, int workerId,
			int checkerId, JSONArray rowkeyList) throws Exception {

		List<TipsDao> tips = queryTipsByWorkState(grids, workStatus,
				checkTaskId, subTaskId, workerId, checkerId, rowkeyList);

		if (tips == null || tips.size() == 0) {

			throw new Exception("没有可抽取的tips");
		}

		Map<String, Integer> map = new HashMap<String, Integer>();
		for (TipsDao json : tips) {

			String type = json.getS_sourceType();

			if (map.containsKey(type)) {
				map.put(type, map.get(type) + 1);
			} else {
				map.put(type, 1);
			}
		}

		return map;

	}

	public static void main(String[] args) {
		double p = (double) (2 * 30) / 100;

		System.out.println(p);
		System.out.println(Math.ceil(p));
	}

}
