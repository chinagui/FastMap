package org.navinfo.dataservice.engine.meta.chain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
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

	/**
	 * 根据kindCode获取chain值
	 * 
	 * @param kindCode
	 * @return
	 * @throws Exception
	 */
	public JSONArray getChainByKindCode(String kindCode) throws Exception {

		Connection conn = DBConnector.getInstance().getMetaConnection();

		try {
			QueryRunner run = new QueryRunner();

			String sql = "select a.chain_name, a.chain_code from sc_point_chain_code a, sc_point_kind_new b where a.chain_code = b.r_kind and b.poikind = ?";

			ResultSetHandler<JSONArray> handler = new ResultSetHandler<JSONArray>() {

				@Override
				public JSONArray handle(ResultSet rs) throws SQLException {

					JSONArray array = new JSONArray();

					while (rs.next()) {
						JSONObject object = new JSONObject();

						object.put("chainCode", rs.getString("chain_code"));

						object.put("chainName", rs.getString("chain_name"));

						array.add(object);
					}

					return array;
				}
			};
			return run.query(conn, sql, handler, kindCode);
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
	public String getLevelByChain(String chain, String kindCode) throws Exception {

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
}
