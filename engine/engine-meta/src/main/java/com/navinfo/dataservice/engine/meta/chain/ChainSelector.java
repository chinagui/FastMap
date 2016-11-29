package com.navinfo.dataservice.engine.meta.chain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

/**
 * e获取chain值
 * 
 * @author zhangxiaolong
 * 
 */
public class ChainSelector {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	
	private Connection conn;
	
	public ChainSelector() {
		
	}
	
	public ChainSelector(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 根据kindCode获取chain值
	 * 1)只返回外业品牌；2)充电站、充电桩品牌返回
	 * @param kindCode
	 * @return
	 * @throws Exception
	 */
	public JSONObject getChainByKindCode(String kindCode) throws Exception {

		Connection conn = DBConnector.getInstance().getMetaConnection();

		try {
			QueryRunner run = new QueryRunner();

			StringBuilder sb = new StringBuilder();

			sb.append(" select a.chain_name, a.chain_code,a.category,a.weight,b.poikind ");

			sb.append(" from sc_point_chain_code a, sc_point_kind_new b ");

			if (kindCode == null || kindCode.length() <= 0) {
				sb.append(" where a.chain_code = b.r_kind and a.type=1 ");
			} else {
				sb.append(" where a.chain_code = b.r_kind and a.type=1 and b.poikind = ? ");
			}

			sb.append("order by b.poikind ");

			ResultSetHandler<JSONObject> handler = new ResultSetHandler<JSONObject>() {

				@Override
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject rootObj = new JSONObject();

					while (rs.next()) {
						JSONObject codeObj = new JSONObject();

						int category = rs.getInt("category");

						codeObj.put("category",
								rs.wasNull() ? "" : String.valueOf(category));

						int weight = rs.getInt("weight");

						codeObj.put("weight",
								rs.wasNull() ? "" : String.valueOf(weight));

						codeObj.put("chainCode", rs.getString("chain_Code"));

						codeObj.put("chainName", rs.getString("chain_Name"));

						String poiKind = rs.getString("poikind");

						if (rootObj.has(poiKind)) {
							JSONArray array = rootObj.getJSONArray(poiKind);

							array.add(codeObj);
						} else {
							JSONArray array = new JSONArray();

							array.add(codeObj);

							rootObj.put(poiKind, array);
						}
					}

					return rootObj;
				}
			};
			if (kindCode == null || kindCode.length() <= 0) {
				return run.query(conn, sb.toString(), handler);
			}
			return run.query(conn, sb.toString(), handler, kindCode);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据chain获取level
	 * 
	 * @param kindCode
	 * @return chain的level
	 * @throws Exception
	 */
	public String getLevelByChain(String chain, String kindCode)
			throws Exception {

		Connection conn = DBConnector.getInstance().getMetaConnection();

		try {
			StringBuilder sb = new StringBuilder();

			sb.append("select a.\"LEVEL\" from sc_fm_control a,sc_point_kind_new b where "
					+ "a.kind_code = b.poikind and b.r_kind = ? and a.kind_code = ?");

			QueryRunner run = new QueryRunner();

			ResultSetHandler<String> handler = new ResultSetHandler<String>() {

				@Override
				public String handle(ResultSet rs) throws SQLException {

					if (rs.next()) {
						return rs.getString("LEVEL");
					} else {
						try {
							throw new Exception("根据chain获取level失败");
						} catch (Exception e) {
						}
					}
					return null;
				}
			};

			return run.query(conn, sb.toString(), handler, chain, kindCode);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询品牌名称
	 * @param chainCode
	 * @return
	 * @throws Exception
	 */
	public JSONObject getChainMap() throws Exception{
		
		String sql = "SELECT distinct chain_code,chain_name FROM SC_POINT_CHAIN_CODE";
		
		ResultSet resultSet = null;
		
		PreparedStatement pstmt = null;
		
		JSONObject chainMap = new JSONObject();
		
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				chainMap.put(resultSet.getString("chain_code"), resultSet.getString("chain_name"));
			}
			return chainMap;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
	}

	public JSONArray getChargingChain() throws Exception {
		Connection conn = null;
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		String sql = "select * from sc_point_charging_chain";
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			JSONArray data = new JSONArray();
			while (resultSet.next()) {
				JSONObject row = new JSONObject();
				row.put("chainCode", resultSet.getString("chain_code"));
				row.put("chainName", resultSet.getString("chain_name"));
				row.put("hm_flag", resultSet.getString("hm_flag"));
				row.put("memo", resultSet.getString("memo"));
				data.add(row);
			}
			return data;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DbUtils.close(conn);
		}
		
	}
	
	
}
