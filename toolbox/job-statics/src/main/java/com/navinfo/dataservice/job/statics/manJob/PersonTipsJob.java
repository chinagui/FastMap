package com.navinfo.dataservice.job.statics.manJob;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: PersonTipsJob
 * @Description: 人天任务-道路-新增里程- tips：统计当天该作业员，该任务的新增测线tips的里程
 *               人天任务-道路-tips量-day/tips：统计当天该作业员，该任务的所有tips量
 *               只统计中线子任务，一个子任务对应一个作业员，按照子任务统计即可
 * @author Zhang Junfang, Gui Yingen
 * @date 2017年11月10日 上午11:36:37
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PersonTipsJob extends AbstractStatJob {

	public PersonTipsJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public String stat() throws JobException {
		PersonTipsJobRequest statReq = (PersonTipsJobRequest) request;
		try {
			String workDay = statReq.getWorkDay();
			log.info("start stat PersonTipsJob");
			// 统计
			Map<Integer, List> resultMap = getSubTaskLineStat(workDay);

			Map<String, List<Map<String, Object>>> result = new HashMap<String, List<Map<String, Object>>>();
			List<Map<String, Object>> resultMapList = new ArrayList<>();
			for (Integer subTaskId : resultMap.keySet()) {
				Map<String, Object> subTaskMap = new HashMap<>();

				List statList = resultMap.get(subTaskId);
				JSONObject statObj = (JSONObject) statList.get(0);
				Map<String, JSONObject> tipsCodeMap = (Map<String, JSONObject>) statList.get(1);

				subTaskMap.put("subtaskId", subTaskId);
				subTaskMap.put("tipsAddLen", statObj.getDouble("tipsAddLen"));
				subTaskMap.put("tipsAllNum", statObj.getDouble("tipsAllNum"));
				subTaskMap.put("tips1Len", statObj.getDouble("tips1Len"));
				subTaskMap.put("tips2Len", statObj.getDouble("tips2Len"));
				subTaskMap.put("tips3Len", statObj.getDouble("tips3Len"));
				subTaskMap.put("tips4Len", statObj.getDouble("tips4Len"));
				subTaskMap.put("tips5Len", statObj.getDouble("tips5Len"));
				subTaskMap.put("tips6Len", statObj.getDouble("tips6Len"));
				subTaskMap.put("tips7Len", statObj.getDouble("tips7Len"));
				subTaskMap.put("tips8Len", statObj.getDouble("tips8Len"));
				subTaskMap.put("tips9Len", statObj.getDouble("tips9Len"));
				subTaskMap.put("tips10Len", statObj.getDouble("tips10Len"));
				subTaskMap.put("workDay", workDay);

				for (String tipsCode : tipsCodeMap.keySet()) {
					JSONObject lifeCycelStatObj = tipsCodeMap.get(tipsCode);
					subTaskMap.put("tips" + tipsCode + "AddNum", lifeCycelStatObj.get("addNum"));
					subTaskMap.put("tips" + tipsCode + "UpNum", lifeCycelStatObj.get("upNum"));
					subTaskMap.put("tips" + tipsCode + "DelNum", lifeCycelStatObj.get("delNum"));
				}

				resultMapList.add(subTaskMap);

				if (subTaskId == 1) {
					System.err.println(subTaskMap);
				}
			}
			result.put("person_tips", resultMapList);
			JSONObject identifyJson = new JSONObject();
			identifyJson.put("timestamp", statReq.getTimestamp());
			identifyJson.put("workDay", statReq.getWorkDay());
			statReq.setIdentifyJson(identifyJson);
			statReq.setIdentify("timestamp:" + statReq.getTimestamp() + ",workDay:" + statReq.getWorkDay());
			log.info("end stat PersonTipsJob");
			return JSONObject.fromObject(result).toString();
		} catch (Exception e) {
			throw new JobException("PersonTipsJob执行报错", e);
		}
	}

	/**
	 * 统计当天子任务的总测线里程和总tips量
	 * 按照tips对应等级统计当天子任务的新增测线里程，且分别统计子任务的增删改量
	 * 
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, List> getSubTaskLineStat(final String timestamp) throws Exception {
		java.sql.Connection orclConn = null;
		try {
			String sqlLineQuery = "SELECT T.S_MSUBTASKID, T.ID, T.WKTLOCATION, T.T_LIFECYCLE, T.S_SOURCETYPE\n" +
					" FROM TIPS_INDEX T\n" +
					" WHERE T.S_MSUBTASKID <> 0 and T.S_SOURCETYPE = '2001'\n" + // s_msubtaskid!=0
					" ORDER BY T.S_MSUBTASKID";
			orclConn = DBConnector.getInstance().getTipsIdxConnection();

			QueryRunner run = new QueryRunner();
			return run.query(orclConn, sqlLineQuery, new ResultSetHandler<Map<Integer, List>>() {
				@Override
				public Map<Integer, List> handle(ResultSet rs) throws SQLException {
					Map<Integer, List> subtaskTipsMap = new HashMap<>();

					Connection hbaseConn = null;
					Table htab = null;
					try {
						hbaseConn = HBaseConnector.getInstance().getConnection();
						 htab =hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

						while (rs.next()) {
							int subtaskId = rs.getInt("S_MSUBTASKID");
							String rowkey = rs.getString("ID");
							String tipsCode = rs.getString("S_SOURCETYPE");

							long tipsAllNum = 0; // tips新增量
							double tipsAddLen = 0; // tips新增测线里程
							double tips1Len = 0; // tips各等级新增测线里程
							double tips2Len = 0;
							double tips3Len = 0;
							double tips4Len = 0;
							double tips5Len = 0;
							double tips6Len = 0;
							double tips7Len = 0;
							double tips8Len = 0;
							double tips9Len = 0;
							double tips10Len = 0;
							long addNum = 0;
							long upNum = 0;
							long delNum = 0;
							List statList = null;
							JSONObject statObj = null;
							Map<String, JSONObject> tipsCodeMap = null;
							JSONObject lifeCycleStatObj = null;

							if (subtaskTipsMap.containsKey(subtaskId)) { // 已有
								statList = subtaskTipsMap.get(subtaskId);
								statObj = (JSONObject) statList.get(0);
								tipsCodeMap = (Map<String, JSONObject>) statList.get(1);

								tipsAllNum = statObj.getLong("tipsAllNum");
								tipsAddLen = statObj.getDouble("tipsAddLen");
								tips1Len = statObj.getDouble("tips1Len");
								tips2Len = statObj.getDouble("tips2Len");
								tips3Len = statObj.getDouble("tips3Len");
								tips4Len = statObj.getDouble("tips4Len");
								tips5Len = statObj.getDouble("tips5Len");
								tips6Len = statObj.getDouble("tips6Len");
								tips7Len = statObj.getDouble("tips7Len");
								tips8Len = statObj.getDouble("tips8Len");
								tips9Len = statObj.getDouble("tips9Len");
								tips10Len = statObj.getDouble("tips10Len");

								if (tipsCodeMap.containsKey(tipsCode)) {
									lifeCycleStatObj = tipsCodeMap.get(tipsCode);
									addNum = lifeCycleStatObj.getLong("addNum");
									upNum = lifeCycleStatObj.getLong("upNum");
									delNum = lifeCycleStatObj.getLong("delNum");
								} else {
									lifeCycleStatObj = new JSONObject();
									tipsCodeMap.put(tipsCode, lifeCycleStatObj);
								}

							} else {
								statObj = new JSONObject();
								tipsCodeMap = new HashMap<>();
								lifeCycleStatObj = new JSONObject();
								tipsCodeMap.put(tipsCode, lifeCycleStatObj);
								statList = new ArrayList();
								statList.add(0, statObj);
								statList.add(1, tipsCodeMap);
								subtaskTipsMap.put(subtaskId, statList);
							}

							JSONObject hbaseTip = HbaseTipsQuery.getHbaseTipsByRowkey(htab, rowkey,
									new String[] { "track", "deep" });
							// 如果是当天子任务
							if (tipsIsNew(hbaseTip, timestamp)) {
								tipsAllNum += 1;

								int lifeCycle = rs.getInt("T_LIFECYCLE");
								if (lifeCycle == 3) { // Tips为新增
									// 分tips状态统计子任务
									addNum += 1;

									// 20170927 新增里程区分测线来源统计
									if ("2001".equals(rs.getString("S_SOURCETYPE"))) {
										// 测线来源 src=0（GPS测线手持端）和2（自绘测线）
										JSONObject deep = hbaseTip.getJSONObject("deep");
										int src = deep.getInt("src");
										if (src != 0 && src != 2) {
											break;
										}
										// 获取测线里程
										double len = deep.getDouble("len");
										// 统计新增测线里程
										tipsAddLen += len;

										// 20171109 按等级统计道路新增里程
										int kind = deep.getInt("kind");
										switch (kind) {
										case 1:
											tips1Len += len;
											break;
										case 2:
											tips2Len += len;
											break;
										case 3:
											tips3Len += len;
											break;
										case 4:
											tips4Len += len;
											break;
										case 5:
											tips5Len += len;
											break;
										case 6:
											tips6Len += len;
											break;
										case 7:
											tips7Len += len;
											break;
										case 8:
											tips8Len += len;
											break;
										case 9:
											tips9Len += len;
											break;
										case 10:
											tips10Len += len;
											break;
										}
									}
								} else if (lifeCycle == 2) {
									upNum += 1;
								} else {
									delNum += 1;
								}
							}
							statObj.put("tipsAllNum", tipsAllNum);
							statObj.put("tipsAddLen", tipsAddLen);
							statObj.put("tips1Len", tips1Len);
							statObj.put("tips2Len", tips2Len);
							statObj.put("tips3Len", tips3Len);
							statObj.put("tips4Len", tips4Len);
							statObj.put("tips5Len", tips5Len);
							statObj.put("tips6Len", tips6Len);
							statObj.put("tips7Len", tips7Len);
							statObj.put("tips8Len", tips8Len);
							statObj.put("tips9Len", tips9Len);
							statObj.put("tips10Len", tips10Len);

							lifeCycleStatObj.put("addNum", addNum);
							lifeCycleStatObj.put("upNum", upNum);
							lifeCycleStatObj.put("delNum", delNum);

						}
					} catch (Exception e) {
						log.error("", e);
						throw new SQLException("PersonTipsJob报错: ", e);
					} finally {
						if (rs != null) {
							rs.close();
						}
						if (htab != null) {
							try {
								htab.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					return subtaskTipsMap;
				}
			});
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(orclConn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(orclConn);
		}
	}

	/**
	 * 判断是否是当天tips
	 * 
	 * @param htab
	 * @param rowkey
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	public boolean tipsIsNew(JSONObject hbaseTip, String timestamp) throws Exception {
		if (hbaseTip.containsKey("track")) {
			JSONObject track = hbaseTip.getJSONObject("track");
			JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");
			if (trackInfoArr != null && trackInfoArr.size() > 0) {
				for (int i = 0; i < trackInfoArr.size(); i++) {
					JSONObject trackInfo = trackInfoArr.getJSONObject(i);
					int stage = trackInfo.getInt("stage");
					String date = trackInfo.getString("date");
					if (stage == 1 && date.startsWith(timestamp)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
