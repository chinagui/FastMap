package com.navinfo.dataservice.engine.meta.chain;

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

/**
 * 获取可为父的fid
 * 
 * @author zhangxiaolong
 *
 */
public class FocusSelector {
	
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	public JSONArray getPoiNum() throws Exception {

		Connection conn = null;

		try {
			conn = DBConnector.getInstance().getMetaConnection();
			String sql = "select distinct poi_num from sc_point_focus where type=2";

			QueryRunner run = new QueryRunner();

			ResultSetHandler<JSONArray> handler = new ResultSetHandler<JSONArray>() {

				@Override
				public JSONArray handle(ResultSet rs) throws SQLException {

					JSONArray array = new JSONArray();

					while (rs.next()) {
						String poiNum = rs.getString("POI_NUM");

						array.add(poiNum);
					}
					return array;
				}
			};

			return run.query(conn, sql, handler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
