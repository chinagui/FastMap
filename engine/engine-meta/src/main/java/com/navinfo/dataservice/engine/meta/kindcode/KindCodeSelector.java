package com.navinfo.dataservice.engine.meta.kindcode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class KindCodeSelector {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private Connection conn;
	
	public KindCodeSelector() {
		
	}
	
	public KindCodeSelector(Connection conn) {
		this.conn = conn;
	}
	

	/**
	 * 用SC_POINT_POICODE_NEW的 CLASS_CODE去重，id与code均为CLASS_CODE，name为CLASS_NAME
	 * 参数：无
	 * 
	 * @return id、code、name
	 * @throws Exception
	 */
	public JSONArray queryTopKindInfo() throws Exception {

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT DISTINCT t.class_code, t.class_name, t.class_code id ");

		sb.append(" FROM sc_point_poicode_new t ");
		
		sb.append(" WHERE t.kg_des<>'GD' ");

		Connection conn = null;
		try {

			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getMetaConnection();

			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				@Override
				public JSONArray handle(ResultSet resultSet)
						throws SQLException {

					JSONArray array = new JSONArray();

					while (resultSet.next()) {
						String id = resultSet.getString("id");

						String code = resultSet.getString("class_code");

						String name = resultSet.getString("class_name");

						JSONObject json = new JSONObject();

						json.put("id", id);

						json.put("code", code);

						json.put("name", name);

						array.add(json);
					}
					return array;
				}
			};

			return run.query(conn, sb.toString(), rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 用SC_POINT_POICODE_NEW的 CLASS_CODE+SUB_CLASS_CODE去重，
	 * id为拼起来的值，code为SUB_CLASS_CODE，name为SUB_CLASS_NAME 参数：topId 大分类id
	 * 返回：id、code、name
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryMediumKindInfo(final String topId) throws Exception {

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT DISTINCT ");

		sb.append(" t.sub_class_code,t.sub_class_name, t.class_code||t.sub_class_code id ");

		sb.append(" FROM sc_point_poicode_new t " + " WHERE t.class_code=? ");
		
		sb.append(" AND t.kg_des<>'GD' ");

		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getMetaConnection();

			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				@Override
				public JSONArray handle(ResultSet resultSet)
						throws SQLException {
					JSONArray array = new JSONArray();

					while (resultSet.next()) {
						String id = resultSet.getString("id");

						String code = resultSet.getString("sub_class_code");

						String name = resultSet.getString("sub_class_name");

						JSONObject json = new JSONObject();

						json.put("topId", topId);

						json.put("id", id);

						json.put("code", code);

						json.put("name", name);

						array.add(json);
					}
					return array;
				}
			};

			return run.query(conn, sb.toString(), rsHandler, topId);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 通过大分类、中分类获取KindCode信息
	 * 
	 * @param topId
	 *            大分类
	 * @param mediumId
	 *            中分类
	 * @return kindCode、kindName、extend
	 * @throws Exception
	 */
	public JSONArray queryKindInfo(String topId, String mediumId, int region)
			throws Exception {

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT t.kind_code,t.kind_name from SC_POINT_POICODE_NEW t");

		sb.append(" WHERE t.class_code=? AND t.sub_class_code=? ");
		
		sb.append(" AND t.kg_des<>'GD' AND t.kind_use<>2 ");

		if (region == 0) {
			sb.append(" and (t.mhm_des='DHM' or t.mhm_des='D') ");
		}

		if (region == 1) {
			sb.append(" and (t.mhm_des='DHM' or t.mhm_des='HM') ");
		}
		
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getMetaConnection();

			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				@Override
				public JSONArray handle(ResultSet resultSet)
						throws SQLException {

					JSONArray array = new JSONArray();

					while (resultSet.next()) {
						String kindCode = resultSet.getString("kind_code");

						String kindName = resultSet.getString("kind_name");

						JSONObject json = new JSONObject();

						json.put("kindCode", kindCode);

						json.put("kindName", kindName);

						json.put("extend", "");

						array.add(json);
					}
					return array;
				}
			};

			return run.query(conn, sb.toString(), rsHandler, topId, mediumId);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 通过region（1：港澳，0：大陆）信息获取KindCode信息
	 * 只返回外业分类(type<>2)
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryKindInfo(int region) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT t.kind_code ,t.kind_name,t.sub_class_code,");
		sb.append(" (select f.\"LEVEL\" from SC_FM_CONTROL f where f.kind_code = t.kind_code) \"level\", ");
		sb.append(" (select f.extend from SC_FM_CONTROL f where f.kind_code = t.kind_code) extend, ");
		sb.append(" (select f.parent from SC_FM_CONTROL f where f.kind_code = t.kind_code) parent, ");
		sb.append(" (select f.chain from SC_FM_CONTROL f where f.kind_code = t.kind_code) chain, ");
		sb.append(" (select f.disp_onlink from SC_FM_CONTROL f where f.kind_code = t.kind_code) disp_onlink ");

		sb.append(" from SC_POINT_POICODE_NEW t  ");
		
		sb.append(" where t.kg_des<>'GD'  and t.kind_use<>2 ");
		
		if (region == 0) {
			sb.append(" and (t.mhm_des='DHM' or t.mhm_des='D') ");
		}

		if (region == 1) {
			sb.append(" and (t.mhm_des='DHM' or t.mhm_des='HM') ");
		}
		
		

		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getMetaConnection();

			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				@Override
				public JSONArray handle(ResultSet resultSet)
						throws SQLException {

					JSONArray array = new JSONArray();

					while (resultSet.next()) {
						JSONObject json = new JSONObject();

						String kindCode = resultSet.getString("kind_code");						
						String kindName = resultSet.getString("kind_name");
						String mediumId = resultSet.getString("sub_class_code");
						String level = resultSet.getString("level");
						int extend = resultSet.getInt("extend");
						int parent = resultSet.getInt("parent");
						int chainFlag = resultSet.getInt("chain");
						int dispOnlink = resultSet.getInt("disp_onlink");
						String strDispOnlink = resultSet.wasNull() ? ""
								: String.valueOf(dispOnlink);

						json.put("kindCode", kindCode);
						json.put("kindName", kindName);
						json.put("mediumId", mediumId);
						json.put("level", level);
						json.put("extend", extend);
						json.put("parent", parent);
						json.put("chainFlag", chainFlag);
						json.put("dispOnlink", strDispOnlink);

						array.add(json);
					}
					return array;
				}
			};

			return run.query(conn, sb.toString(), rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据kindCode获取level
	 * 
	 * @auth zhaokk
	 * @param kindCode
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject searchkindLevel(String kindCode) throws Exception {

		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT chain,kind_code,\"LEVEL\",\"EXTEND\" ");
		builder.append(" FROM sc_fm_control ");
		builder.append(" WHERE kind_code = :1");
		Connection conn = DBConnector.getInstance().getMetaConnection();
		try {
			QueryRunner runner = new QueryRunner();
			return runner.query(DBConnector.getInstance().getMetaConnection(),
					builder.toString(), new ResultSetHandler<JSONObject>() {
						JSONObject jsonObject = new JSONObject();

						@Override
						public JSONObject handle(ResultSet rs)
								throws SQLException {
							if (rs.next()) {
								jsonObject.put("chainFlag", rs.getInt("chain"));
								jsonObject.put("kindId",
										rs.getString("kind_code"));
								jsonObject.put("extend", rs.getString("extend"));
								jsonObject.put("level", rs.getString("level"));
							}
							return jsonObject;
						}
					}, kindCode);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public JSONObject getKindCodeMap() throws Exception {
		
		String sql = "select distinct t.kind_code,t.kind_name from SC_POINT_POICODE_NEW t";
		
		sql +=" where  t.kg_des<>'GD' and t.kind_use<>2 ";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		JSONObject kindCodeMap = new JSONObject();
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				kindCodeMap.put(resultSet.getString("kind_code"), resultSet.getString("kind_name"));
			}
			
			return kindCodeMap;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}
	/**
	 * SELECT DISTINCT KIND_CODE FROM SC_POINT_POICODE_NEW WHERE MHM_DES LIKE '%D%' AND KIND_USE=1
	 * 大陆的kind列表
	 * @return List<String>：KIND_CODE列表
	 * @throws Exception
	 */
	public List<String> getKindCodeDList() throws Exception {	
		String sql = "select distinct t.kind_code from SC_POINT_POICODE_NEW t";
		sql +=" where MHM_DES LIKE '%D%' AND KIND_USE=1";		
		ResultSet resultSet = null;		
		PreparedStatement pstmt = null;		
		List<String> kindCodeMap = new ArrayList<String>();		
		try {
			pstmt = conn.prepareStatement(sql);			
			resultSet = pstmt.executeQuery();			
			while (resultSet.next()) {
				kindCodeMap.add(resultSet.getString("kind_code"));
			}			
			return kindCodeMap;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 根据kindCode获取KIND_NAME
	 * 
	 * @auth gaopengrong
	 * @param kindCode
	 * @return KIND_NAME
	 * @throws Exception
	 */
	public String searchKindName(String kindCode) throws Exception {

		Connection conn = DBConnector.getInstance().getMetaConnection();
		String sql = "select t.kind_name from SC_POINT_POICODE_NEW t where t.kind_code=:1";
		
		ResultSet resultSet = null;
		
		String kindName="";
		
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, kindCode);
			
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				kindName=resultSet.getString("kind_name");
			}
			return kindName;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
