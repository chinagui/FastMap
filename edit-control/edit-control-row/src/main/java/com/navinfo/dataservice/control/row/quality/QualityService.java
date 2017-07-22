package com.navinfo.dataservice.control.row.quality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.editplus.utils.AdFaceSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class QualityService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private QualityService() {
	}

	private static class SingletonHolder {
		private static final QualityService INSTANCE = new QualityService();
	}

	public static QualityService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 获取质检问题属性值
	 * @param userId
	 * @param pid
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 * 备注：
	 * 		当状态为删除或者修改时（即：state ！= 1），需要在查询常规子任务id时查询出subtask表中的EXE_USER_ID，
	 * 		当查询不到履历时，再根据EXE_USER_ID去user_info表中查询出UserRealName并返回，
	 * 		时间则返回系统时间
	 */
	public JSONObject queryInitValueForProblem(long userId, int pid, int subtaskId) throws Exception {
		Connection regiondbConn = null;
		Connection manConn = null;
		try {
			// 获取当前poi的省份、城市
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			int dbId = subtask.getDbId();

			regiondbConn = DBConnector.getInstance().getConnectionById(dbId);
			manConn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			StringBuilder getGeometryBuilder = new StringBuilder();
			getGeometryBuilder.append("SELECT geometry FROM ix_poi WHERE pid = ");
			getGeometryBuilder.append(pid);

			Geometry geometry = run.query(regiondbConn, getGeometryBuilder.toString(),
					new ResultSetHandler<Geometry>() {
						public Geometry handle(ResultSet rs) throws SQLException {
							while (rs.next()) {
								try {
									STRUCT struct = (STRUCT) rs.getObject("geometry");
									String clobStr = GeoTranslator.struct2Wkt(struct);
									return GeoTranslator.wkt2Geometry(clobStr);
								} catch (Exception e1) {
									e1.printStackTrace();
								}
							}
							return null;
						}
					});
			int adminCode = new AdFaceSelector(regiondbConn).getAminIdByGeometry(geometry);
			String adminCodeStr = String.valueOf(adminCode);
			// 省份、城市的json
			JSONObject resultJson = metadataApi.getProvinceAndCityByAdminCode(adminCodeStr);
			if(!resultJson.containsKey("province")){
				resultJson.put("province", "");
			}
			if(!resultJson.containsKey("city")){
				resultJson.put("city", "");
			}
			// 当前poi状态
			LogReader logRead = new LogReader(regiondbConn);
			int state = logRead.getObjectState(pid, "IX_POI");
			StringBuilder queryUserIdAndOpDtSql = new StringBuilder();
			queryUserIdAndOpDtSql.append("SELECT US_ID, OP_DT");
			queryUserIdAndOpDtSql.append("	FROM (SELECT LA.US_ID, LO.OP_DT");
			queryUserIdAndOpDtSql.append("		FROM LOG_ACTION LA, LOG_OPERATION LO, LOG_DETAIL LD");
			queryUserIdAndOpDtSql.append("		WHERE LA.ACT_ID = LO.ACT_ID");
			queryUserIdAndOpDtSql.append("		AND LO.OP_ID = LD.OP_ID");
			queryUserIdAndOpDtSql.append("		AND LD.OB_PID = ");
			queryUserIdAndOpDtSql.append(pid);
			if (state != 1) {
				String queryStkIdSql = "SELECT SUBTASK_ID FROM SUBTASK WHERE QUALITY_SUBTASK_ID = ?";
				int StkId = run.queryForInt(manConn, queryStkIdSql, subtaskId);
				queryUserIdAndOpDtSql.append("		AND LA.STK_ID = ");
				queryUserIdAndOpDtSql.append(StkId);
				queryUserIdAndOpDtSql.append("		ORDER BY LO.OP_DT DESC)");
				queryUserIdAndOpDtSql.append("WHERE ROWNUM = 1");
				JSONObject userIdAndTimeJson = run.query(regiondbConn, queryUserIdAndOpDtSql.toString(),
						userIdAndTimeHandler);
				long usId = userIdAndTimeJson.getLong("usId");
				// 当usId = 0时，采集员姓名返回空串
				resultJson.put("usId", usId);
				UserInfo userInfoByUserId = apiService.getUserInfoByUserId(usId);
				String collectorUser = userInfoByUserId.getUserRealName();
				resultJson.put("collectorUser", collectorUser == null ? "" : collectorUser);
				long currentTime = userIdAndTimeJson.getLong("opDt");
				resultJson.put("collectorTime", currentTime == 0 ? "" : DateUtils.longToString(currentTime, "yyyy.MM.dd"));
			} else {
				queryUserIdAndOpDtSql.append("		ORDER BY LO.OP_DT DESC)");
				queryUserIdAndOpDtSql.append("WHERE ROWNUM = 1");
				JSONObject userIdAndTimeJson = run.query(regiondbConn, queryUserIdAndOpDtSql.toString(),
						userIdAndTimeHandler);
				long usId = userIdAndTimeJson.getLong("usId");
				String collectorUser = null;
				long currentTime = 0L;
				if (usId == userId) {
					collectorUser = "AAA";
					currentTime = System.currentTimeMillis();
				} else {
					collectorUser = apiService.getUserInfoByUserId(usId).getUserRealName();
					currentTime = userIdAndTimeJson.getLong("opDt");
				}
				resultJson.put("collectorUser", collectorUser == null ? "" : collectorUser);
				resultJson.put("collectorTime", currentTime == 0 ? "" : DateUtils.longToString(currentTime, "yyyy.MM.dd"));
				resultJson.put("usId", usId);
			}
			return resultJson;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(regiondbConn);
			DbUtils.rollbackAndCloseQuietly(manConn);
			log.error("获取质检问题失败，原因为：" + e.getMessage());
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(regiondbConn);
			DbUtils.commitAndCloseQuietly(manConn);
		}
	}

	/**
	 * userId和采集时间的结果集处理器
	 * 当查询不到结果时，则usId = 0   并且   opDt = 0（最终时间返回：1970.01.01） 
	 */
	ResultSetHandler<JSONObject> userIdAndTimeHandler = new ResultSetHandler<JSONObject>() {
		public JSONObject handle(ResultSet rs) throws SQLException {
			JSONObject object = new JSONObject();
			if (rs.next()) {
				object.put("usId", rs.getLong("us_id"));
				object.put("opDt", rs.getTimestamp("op_dt").getTime());
			} else {
				object.put("usId", 0L);
				object.put("opDt", 0L);
			}
			return object;
		}
	};

	/**
	 * poi质检问题操作（新增，修改，删除）
	 * @param userId
	 * @param dataJson
	 * @throws Exception
	 */
	public void operateProblem(long userId, JSONObject dataJson) throws Exception {
		Connection checkConn = null;
		Connection manConn = null;
		try {
			String command = null;
			checkConn = DBConnector.getInstance().getCheckConnection();
			manConn = DBConnector.getInstance().getManConnection();
			if (dataJson.containsKey("command")) {
				command = dataJson.getString("command");
			} else {
				throw new Exception("参数异常：没找到command。");
			}
			QueryRunner run = new QueryRunner();
			if ("DELETE".equals(command)) {
				String problemNum = null;
				if (dataJson.containsKey("problemNum")) {
					problemNum = dataJson.getString("problemNum");
				} else {
					throw new Exception("参数异常：没找到problemNum");
				}
				StringBuilder builder = new StringBuilder();
				builder.append("DELETE FROM POI_PROBLEM_SUMMARY WHERE PROBLEM_NUM = '");
				builder.append(problemNum + "'");
				String deleteSql = builder.toString();
				log.info("delete sql :" + deleteSql);
				run.update(checkConn, deleteSql);

			} else if ("UPDATE".equals(command)) {
				JSONObject data = new JSONObject();
				if (dataJson.containsKey("data")) {
					data = dataJson.getJSONObject("data");
				} else {
					throw new Exception("参数异常：没找到data。");
				}
				
				List<String> conditions = new ArrayList<>();
				List<String> params = new ArrayList<>();
				String problemNum = "";
				String checkMode = "";
				String group = "";
				String classBottom = "";
				String problemType = "";
				String problemPhenomenon = "";
				String problemLevel = "";
				String problemDescription = "";
				String intialCause = "";
				String rootCause = "";
				String memo = "";
				String version = "";
				String confirmUser = "";
				
				if(data.containsKey("problemNum")){
					problemNum = data.getString("problemNum");
				}
				if(data.containsKey("checkMode")){
					checkMode = data.getString("checkMode");
				}
				if(!StringUtils.isEmpty(checkMode)){
					conditions.add("CHECK_MODE = ?");
					params.add(checkMode);
				}
				if(data.containsKey("group")){
					group = data.getString("group");
				}
				if(!StringUtils.isEmpty(group)){
					conditions.add("\"GROUP\" = ?");
					params.add(group);
				}
				if(data.containsKey("classBottom")){
					classBottom = data.getString("classBottom");
				}
				if(!StringUtils.isEmpty(classBottom)){
					conditions.add("CLASS_BOTTOM = ?");
					params.add(classBottom);
				}
				if(data.containsKey("problemType")){
					problemType = data.getString("problemType");
				}
				if(!StringUtils.isEmpty(problemType)){
					conditions.add("PROBLEM_TYPE = ?");
					params.add(problemType);
				}
				if(data.containsKey("problemPhenomenon")){
					problemPhenomenon = data.getString("problemPhenomenon");
				}
				if(!StringUtils.isEmpty(problemPhenomenon)){
					conditions.add("PROBLEM_PHENOMENON = ?");
					params.add(problemPhenomenon);
				}
				if(data.containsKey("problemLevel")){
					problemLevel = data.getString("problemLevel");
				}
				if(!StringUtils.isEmpty(problemLevel)){
					conditions.add("PROBLEM_LEVEL = ?");
					params.add(problemLevel);
				}
				if(data.containsKey("problemDescription")){
					problemDescription = data.getString("problemDescription");
				}
				if(!StringUtils.isEmpty(problemDescription)){
					conditions.add("PROBLEM_DESCRIPTION = ?");
					params.add(problemDescription);
				}
				if(data.containsKey("intialCause")){
					intialCause = data.getString("intialCause");
				}
				if(!StringUtils.isEmpty(intialCause)){
					conditions.add("INITIAL_CAUSE = ?");
					params.add(intialCause);
				}
				if(data.containsKey("rootCause")){
					rootCause = data.getString("rootCause");
				}
				if(!StringUtils.isEmpty(rootCause)){
					conditions.add("ROOT_CAUSE = ?");
					params.add(rootCause);
				}
				if(data.containsKey("memo")){
					memo = data.getString("memo");
				}
				if(!StringUtils.isEmpty(memo)){
					conditions.add("MEMO = ?");
					params.add(memo);
				}
				if(data.containsKey("version")){
					version = data.getString("version");
				}
				if(!StringUtils.isEmpty(version)){
					conditions.add("\"VERSION\" = ?");
					params.add(version);
				}
				if(data.containsKey("confirmUser")){
					confirmUser = data.getString("confirmUser");
				}
				if(!StringUtils.isEmpty(confirmUser)){
					conditions.add("CONFIRM_USER = ?");
					params.add(confirmUser);
				}
				
				StringBuilder builder = new StringBuilder();
				builder.append("UPDATE POI_PROBLEM_SUMMARY ");
				if(conditions.size() > 0){
					builder.append("SET ");
					for (String con : conditions) {
						builder.append(con).append(",");
					}
				} else {
					throw new Exception("数据更新异常：没有可更新的数据。");
				}
				String updateSql = builder.deleteCharAt(builder.length() - 1).append(" WHERE PROBLEM_NUM = ?").toString();
				params.add(problemNum);
				run.update(checkConn, updateSql, params.toArray());
			} else if ("ADD".equals(command)) {
				JSONObject data = new JSONObject();
				if (dataJson.containsKey("data")) {
					data = dataJson.getJSONObject("data");
				} else {
					throw new Exception("参数异常：没找到data");
				}
				
				SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
				List<String> conditions = new ArrayList<>();
				List<Object> params = new ArrayList<>();
				String group = "";
				String province = "";
				String city = "";
				int subtaskId = 0;
				String level = "";
				int meshId = 0;
				String poiNum = "";
				String kindCode = "";
				String classTop = "";
				String classMedium = "";
				String classBottom = "";
				String problemType = "";
				String problemPhenomenon = "";
				String problemDescription = "";
				String intialCause = "";
				String rootCause = "";
				String collectorUser = "";
				String collectorTime = "";
				String checkMode = "";
				String confirmUser = "";
				String version = "";
				String problemLevel = "";
				String memo = "";
				
				if(data.containsKey("group")){
					group = data.getString("group");
				}
				if(!StringUtils.isEmpty(group)){
					conditions.add("\"GROUP\"");
					params.add(group);
				}
				if(data.containsKey("province")){
					province = data.getString("province");
				}
				if(!StringUtils.isEmpty(province)){
					conditions.add("PROVINCE");
					params.add(province);
				}
				if(data.containsKey("city")){
					city = data.getString("city");
				}
				if(!StringUtils.isEmpty(city)){
					conditions.add("CITY");
					params.add(city);
				}
				if(data.containsKey("subtaskId")){
					subtaskId = data.getInt("subtaskId");
				}
				if(subtaskId != 0){
					conditions.add("SUBTASK_ID");
					params.add(subtaskId);
				}
				if(data.containsKey("level")){
					level = data.getString("level");
				}
				if(!StringUtils.isEmpty(level)){
					conditions.add("\"LEVEL\"");
					params.add(level);
				}
				if(data.containsKey("meshId")){
					meshId = data.getInt("meshId");
				}
				if(meshId != 0){
					conditions.add("MESH_ID");
					params.add(meshId);
				}
				if(data.containsKey("poiNum")){
					poiNum = data.getString("poiNum");
				}
				if(!StringUtils.isEmpty(poiNum)){
					conditions.add("POI_NUM");
					params.add(poiNum);
				}
				if(data.containsKey("kindCode")){
					kindCode = data.getString("kindCode");
				}
				if(!StringUtils.isEmpty(kindCode)){
					conditions.add("KIND_CODE");
					params.add(kindCode);
				}
				if(data.containsKey("classTop")){
					classTop = data.getString("classTop");
				}
				if(!StringUtils.isEmpty(classTop)){
					conditions.add("CLASS_TOP");
					params.add(classTop);
				}
				if(data.containsKey("classMedium")){
					classMedium = data.getString("classMedium");
				}
				if(!StringUtils.isEmpty(classMedium)){
					conditions.add("CLASS_MEDIUM");
					params.add(classMedium);
				}
				if(data.containsKey("classBottom")){
					classBottom = data.getString("classBottom");
				}
				if(!StringUtils.isEmpty(classBottom)){
					conditions.add("CLASS_BOTTOM");
					params.add(classBottom);
				}
				if(data.containsKey("problemType")){
					problemType = data.getString("problemType");
				}
				if(!StringUtils.isEmpty(problemType)){
					conditions.add("PROBLEM_TYPE");
					params.add(problemType);
				}
				if(data.containsKey("problemPhenomenon")){
					problemPhenomenon = data.getString("problemPhenomenon");
				}
				if(!StringUtils.isEmpty(problemPhenomenon)){
					conditions.add("PROBLEM_PHENOMENON");
					params.add(problemPhenomenon);
				}
				if(data.containsKey("problemDescription")){
					problemDescription = data.getString("problemDescription");
				}
				if(!StringUtils.isEmpty(problemDescription)){
					conditions.add("PROBLEM_DESCRIPTION");
					params.add(problemDescription);
				}
				if(data.containsKey("intialCause")){
					intialCause = data.getString("intialCause");
				}
				if(!StringUtils.isEmpty(intialCause)){
					conditions.add("INITIAL_CAUSE");
					params.add(intialCause);
				}
				if(data.containsKey("rootCause")){
					rootCause = data.getString("rootCause");
				}
				if(!StringUtils.isEmpty(rootCause)){
					conditions.add("ROOT_CAUSE");
					params.add(rootCause);
				}
				if(data.containsKey("collectorUser")){
					collectorUser = data.getString("collectorUser");
				}
				if(!StringUtils.isEmpty(collectorUser)){
					conditions.add("COLLECTOR_USER");
					params.add(collectorUser);
				}
				if(data.containsKey("collectorTime")){
					collectorTime = data.getString("collectorTime");
				}
				if(!StringUtils.isEmpty(collectorTime)){
					conditions.add("COLLECTOR_TIME");
					params.add(new Timestamp(df.parse(collectorTime).getTime()));
				}
				if(data.containsKey("checkMode")){
					checkMode = data.getString("checkMode");
				}
				if(!StringUtils.isEmpty(checkMode)){
					conditions.add("CHECK_MODE");
					params.add(checkMode);
				}
				if(data.containsKey("confirmUser")){
					confirmUser = data.getString("confirmUser");
				}
				if(!StringUtils.isEmpty(confirmUser)){
					conditions.add("CONFIRM_USER");
					params.add(confirmUser);
				}
				if(data.containsKey("version")){
					version = data.getString("version");
				}
				if(!StringUtils.isEmpty(version)){
					conditions.add("\"VERSION\"");
					params.add(version);
				}
				if(data.containsKey("problemLevel")){
					problemLevel = data.getString("problemLevel");
				}
				if(!StringUtils.isEmpty(problemLevel)){
					conditions.add("PROBLEM_LEVEL");
					params.add(problemLevel);
				}
				if(data.containsKey("memo")){
					memo = data.getString("memo");
				}
				if(!StringUtils.isEmpty(memo)){
					conditions.add("MEMO");
					params.add(memo);
				}
				
				String problemNum = UUID.randomUUID().toString().replaceAll("-", "");
				conditions.add("PROBLEM_NUM");
				params.add(problemNum);
				long checkUser = userId;
				conditions.add("CHECK_USER");
				params.add(checkUser);
				Timestamp checkTime = new Timestamp(System.currentTimeMillis());
				conditions.add("CHECK_TIME");
				params.add(checkTime);
				conditions.add("MODIFY_DATE");
				params.add(checkTime);
				conditions.add("MODIFY_USER");
				params.add(String.valueOf(checkUser));
				if("AAA".equals(collectorUser)){
					conditions.add("MEMO_USER");
					StringBuilder sb = new StringBuilder();
					sb.append("SELECT EXE_USER_ID FROM SUBTASK WHERE QUALITY_SUBTASK_ID = ").append(subtaskId);
					int memoUser = run.queryForInt(manConn, sb.toString());
					params.add(String.valueOf(memoUser));
				} else {
					conditions.add("MEMO_USER");
					params.add(collectorUser);
				}
				
				StringBuilder builder = new StringBuilder();
				builder.append("INSERT INTO POI_PROBLEM_SUMMARY (");
				if(conditions.size() > 0){
					for (String con : conditions) {
						builder.append(con).append(" ,");
					}
				} else {
					throw new Exception("数据更新异常：没有可新增的数据。");
				}
				
				builder.deleteCharAt(builder.length() - 1).append(") VALUES (");
				for (int i = 0; i < conditions.size(); i++) {
					builder.append(" ? ,");
				}
				builder.deleteCharAt(builder.length() - 1).append(")");
				run.update(checkConn, builder.toString(), params.toArray());
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(checkConn);
			DbUtils.rollbackAndCloseQuietly(manConn);
			log.error("poi质检问题操作失败，原因为：" + e.getMessage());
			e.printStackTrace();
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(checkConn);
			DbUtils.commitAndCloseQuietly(manConn);
		}
	}

	/**
	 * poi质检问题查看
	 * @param dataJson
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryProblemList(JSONObject dataJson) throws Exception {
		Connection checkConn = null;
		Connection manConn = null;
		PreparedStatement preparedStatementCheck = null;
		ResultSet resultSetCheck = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			checkConn = DBConnector.getInstance().getCheckConnection();
			manConn = DBConnector.getInstance().getManConnection();
			
			List<Object> params = new ArrayList<>();
			String poiNum = dataJson.getString("poiNum");
			int subtaskId = dataJson.getInt("subtaskId");
			params.add(poiNum);
			params.add(subtaskId);
			
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT PROBLEM_NUM, PROVINCE, CITY, \"GROUP\", KIND_CODE, MESH_ID, PROBLEM_LEVEL, PROBLEM_DESCRIPTION, ");
			builder.append("INITIAL_CAUSE, ROOT_CAUSE, COLLECTOR_USER, COLLECTOR_TIME, MEMO, \"VERSION\", CONFIRM_USER, CHECK_USER, ");
			builder.append("CHECK_TIME, CHECK_MODE, CLASS_MEDIUM, CLASS_BOTTOM, PROBLEM_TYPE, PROBLEM_PHENOMENON  FROM POI_PROBLEM_SUMMARY WHERE POI_NUM = ? AND SUBTASK_ID = ? ");
			
			String classTop = "";
			String classMedium = "";
			if(dataJson.containsKey("classTop")){
				classTop = dataJson.getString("classTop");
			}
			if(dataJson.containsKey("classMedium")){
				classMedium = dataJson.getString("classMedium");
			}
			if(!StringUtils.isEmpty(classTop)){
				builder.append("AND CLASS_TOP = ? ");
				params.add(classTop);
			}
			if(!StringUtils.isEmpty(classMedium)){
				builder.append("AND CLASS_MEDIUM = ? ");
				params.add(classMedium);
			}
			
			preparedStatementCheck = checkConn.prepareStatement(builder.toString());
			for(int i = 0 ; i < params.size(); i ++){
				preparedStatementCheck.setObject(i + 1, params.get(i));
			}
			resultSetCheck = preparedStatementCheck.executeQuery();
			JSONArray resultJson = new JSONArray();
			Map<String, String> map = metadataApi.getKindNameByKindCode();
			while (resultSetCheck.next()) {
				JSONObject jo = new JSONObject();
				jo.put("problemNum", resultSetCheck.getString("PROBLEM_NUM") != null ? resultSetCheck.getString("PROBLEM_NUM") : "");
				jo.put("province", resultSetCheck.getString("PROVINCE")  != null ? resultSetCheck.getString("PROVINCE") : "");
				jo.put("city", resultSetCheck.getString("CITY")  != null ? resultSetCheck.getString("CITY") : "");
				jo.put("group", resultSetCheck.getString("GROUP")  != null ? resultSetCheck.getString("GROUP") : "");
				String kindCode = resultSetCheck.getString("KIND_CODE");
				jo.put("kindCode", kindCode != null ? kindCode : "");
				String kindName = null;
				kindName = map.get(kindCode);
				jo.put("kindName", kindName != null ? kindName : "");
				jo.put("meshId", resultSetCheck.getInt("MESH_ID"));
				jo.put("problemLevel", resultSetCheck.getString("PROBLEM_LEVEL") != null ? resultSetCheck.getString("PROBLEM_LEVEL") : "");
				jo.put("problemDescription", resultSetCheck.getString("PROBLEM_DESCRIPTION") != null ? resultSetCheck.getString("PROBLEM_DESCRIPTION") : "");
				jo.put("intialCause", resultSetCheck.getString("INITIAL_CAUSE") != null ? resultSetCheck.getString("INITIAL_CAUSE") : "");
				jo.put("rootCause", resultSetCheck.getString("ROOT_CAUSE") != null ? resultSetCheck.getString("ROOT_CAUSE") : "");
				jo.put("collectorUser", resultSetCheck.getString("COLLECTOR_USER") != null ? resultSetCheck.getString("COLLECTOR_USER") : "");
				Timestamp ts = resultSetCheck.getTimestamp("COLLECTOR_TIME");
				if(ts != null){
					jo.put("collectorTime", DateUtils.longToString(ts.getTime(), "yyyy.MM.dd"));
				} else {
					jo.put("collectorTime", "");
				}
				jo.put("memo", resultSetCheck.getString("MEMO") != null ? resultSetCheck.getString("MEMO") : "");
				jo.put("version", resultSetCheck.getString("VERSION") != null ? resultSetCheck.getString("VERSION") : "");
				jo.put("confirmUser", resultSetCheck.getString("CONFIRM_USER") != null ? resultSetCheck.getString("CONFIRM_USER") : "");
				int checkUserId = resultSetCheck.getInt("CHECK_USER");
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT USER_REAL_NAME FROM USER_INFO WHERE USER_ID = ").append(checkUserId);
				preparedStatement = manConn.prepareStatement(sb.toString());
				resultSet = preparedStatement.executeQuery();
				String checkUser = null;
				if(resultSet.next()){
					checkUser = resultSet.getString(1);
				}
				jo.put("checkUser", checkUser != null ? checkUser : "");
				jo.put("checkTime", DateUtils.longToString(resultSetCheck.getTimestamp("CHECK_TIME").getTime(), "yyyy.MM.dd"));
				jo.put("checkMode", resultSetCheck.getString("CHECK_MODE") != null ? resultSetCheck.getString("CHECK_MODE") : "");
				jo.put("classMedium", resultSetCheck.getString("CLASS_MEDIUM") != null ? resultSetCheck.getString("CLASS_MEDIUM") : "");
				jo.put("classBottom", resultSetCheck.getString("CLASS_BOTTOM") != null ? resultSetCheck.getString("CLASS_BOTTOM") : "");
				jo.put("problemType", resultSetCheck.getString("PROBLEM_TYPE") != null ? resultSetCheck.getString("PROBLEM_TYPE") : "");
				jo.put("problemPhenomenon", resultSetCheck.getString("PROBLEM_PHENOMENON") != null ? resultSetCheck.getString("PROBLEM_PHENOMENON") : "");
				resultJson.add(jo);
			}
			return resultJson;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(checkConn);
			DbUtils.rollbackAndCloseQuietly(manConn);
			log.error("获取poi质检问题失败，原因为：" + e.getMessage());
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(checkConn);
			DbUtils.commitAndCloseQuietly(manConn);
			DbUtils.closeQuietly(preparedStatementCheck);
			DbUtils.closeQuietly(resultSetCheck);
			DbUtils.closeQuietly(preparedStatement);
			DbUtils.closeQuietly(resultSet);
		}
	}
}
