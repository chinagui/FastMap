package com.navinfo.dataservice.engine.meta.chain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

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
	public JSONObject getChainByKindCode(String kindCode) throws Exception {

		Connection conn = DBConnector.getInstance().getMetaConnection();

		try {
			QueryRunner run = new QueryRunner();

			StringBuilder sb = new StringBuilder();

			sb.append(" select a.chain_name, a.chain_code,a.category,a.weight,b.poikind ");

			sb.append(" from sc_point_chain_code a, sc_point_kind_new b ");

			if (kindCode == null || kindCode.length() <= 0) {
				sb.append(" where a.chain_code = b.r_kind ");
			} else {
				sb.append(" where a.chain_code = b.r_kind and b.poikind = ? ");
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
}
