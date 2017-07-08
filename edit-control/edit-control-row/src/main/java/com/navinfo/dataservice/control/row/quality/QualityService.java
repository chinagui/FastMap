package com.navinfo.dataservice.control.row.quality;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
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

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class QualityService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private QualityService() {}
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
	public JSONObject queryInitValueForProblem(long userId, int pid, int subtaskId)  throws Exception{
		Connection regiondbConn = null;
		Connection manConn = null;
		try {
			//获取当前poi的省份、城市
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			MetadataApi metadataApi=(MetadataApi)ApplicationContextUtil.getBean("metadataApi");
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			int dbId = subtask.getDbId();
			
			regiondbConn = DBConnector.getInstance().getConnectionById(dbId);
			manConn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			StringBuilder getGeometryBuilder = new StringBuilder();
			getGeometryBuilder.append("SELECT geometry FROM ix_poi WHERE pid = ");
			getGeometryBuilder.append(pid);

			Geometry geometry = run.query(regiondbConn,getGeometryBuilder.toString(), new ResultSetHandler<Geometry>(){
				@Override
				public Geometry handle(ResultSet rs) throws SQLException {
					while (rs.next()) {
						try {
							STRUCT struct=(STRUCT)rs.getObject("geometry");
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
			//省份、城市的json
			JSONObject resultJson = metadataApi.getProvinceAndCityByAdminCode(adminCodeStr);
			//当前poi状态
			LogReader logRead = new LogReader(regiondbConn);
			int state = logRead.getObjectState(pid, "IX_POI");
			if(state != 1 ){
				String queryStkIdSql = "SELECT SUBTASK_ID FROM SUBTASK WHERE QUALITY_SUBTASK_ID = ?";
				int StkId = run.queryForInt(manConn, queryStkIdSql, subtaskId);
				StringBuilder queryUserIdAndOpDtSql = new StringBuilder();
				queryUserIdAndOpDtSql.append("SELECT US_ID, OP_DT");
				queryUserIdAndOpDtSql.append("	FROM (SELECT LA.US_ID, LO.OP_DT");
				queryUserIdAndOpDtSql.append("		FROM LOG_ACTION LA, LOG_OPERATION LO, LOG_DETAIL LD");
				queryUserIdAndOpDtSql.append("		WHERE LA.ACT_ID = LO.ACT_ID");
				queryUserIdAndOpDtSql.append("		AND LO.OP_ID = LD.OP_ID");
				queryUserIdAndOpDtSql.append("		AND LA.STK_ID = ");
				queryUserIdAndOpDtSql.append(StkId);
				queryUserIdAndOpDtSql.append("		AND LD.OB_PID = ");
				queryUserIdAndOpDtSql.append(pid);
				queryUserIdAndOpDtSql.append("		ORDER BY LO.OP_DT DESC)");
				queryUserIdAndOpDtSql.append("WHERE ROWNUM = 1");
				JSONObject userIdAndTimeJson = run.query(regiondbConn, queryUserIdAndOpDtSql.toString(), userIdAndTimeHandler);
				long usId = userIdAndTimeJson.getLong("usId");
				UserInfo userInfoByUserId = apiService.getUserInfoByUserId(usId);
				String collectorUser = userInfoByUserId.getUserRealName();
				long currentTime = userIdAndTimeJson.getLong("opDt");
				String collectorTime = DateUtils.longToString(currentTime, "yyyy.MM.dd");
				//当usId = 0时，采集员姓名返回字符串null
				resultJson.put("collectorUser", collectorUser == null ? "null" : collectorUser);
				resultJson.put("collectorTime", collectorTime);
				resultJson.put("usId", usId);
			} else {
				StringBuilder queryUserIdAndOpDtSql = new StringBuilder();
				queryUserIdAndOpDtSql.append("SELECT US_ID, OP_DT");
				queryUserIdAndOpDtSql.append("	FROM (SELECT LA.US_ID, LO.OP_DT");
				queryUserIdAndOpDtSql.append("		FROM LOG_ACTION LA, LOG_OPERATION LO, LOG_DETAIL LD");
				queryUserIdAndOpDtSql.append("		WHERE LA.ACT_ID = LO.ACT_ID");
				queryUserIdAndOpDtSql.append("		AND LO.OP_ID = LD.OP_ID");
				queryUserIdAndOpDtSql.append("		AND LD.OB_PID = ");
				queryUserIdAndOpDtSql.append(pid);
				queryUserIdAndOpDtSql.append("		ORDER BY LO.OP_DT DESC)");
				queryUserIdAndOpDtSql.append("WHERE ROWNUM = 1");
				JSONObject userIdAndTimeJson = run.query(regiondbConn, queryUserIdAndOpDtSql.toString(), userIdAndTimeHandler);
				long usId = userIdAndTimeJson.getLong("usId");
				String collectorUser = null;
				long currentTime = 0L;
				if(usId == userId){
					collectorUser = "AAA";
					currentTime = System.currentTimeMillis();
				} else {
					collectorUser = apiService.getUserInfoByUserId(usId).getUserRealName();
					currentTime = userIdAndTimeJson.getLong("opDt");
				}
				String collectorTime = DateUtils.longToString(currentTime, "yyyy.MM.dd");
				resultJson.put("collectorUser", collectorUser == null ? "null" : collectorUser);
				resultJson.put("collectorTime", collectorTime);
				resultJson.put("usId", usId);
			}
			return resultJson;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(regiondbConn);
			DbUtils.rollbackAndCloseQuietly(manConn);
			log.error("获取质检问题失败，原因为：" + e.getMessage());
			throw e;
		}finally{
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
			JSONObject object  = new JSONObject();
			if(rs.next()){
				object.put("usId", rs.getLong("us_id"));
				object.put("opDt", rs.getTimestamp("op_dt").getTime());
			} else {
				object.put("usId", 0L);
				object.put("opDt", System.currentTimeMillis());
			}
			return object;
		}
	};
}