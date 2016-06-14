package org.navinfo.dataservice.engine.meta.chain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;

/**
 * 获取可为父的fid
 * 
 * @author zhangxiaolong
 *
 */
public class FocusSelector {
	public JSONArray getPoiNum() throws Exception {

		String sql = "select distinct poi_num from sc_point_focus where type=2";

		QueryRunner run = new QueryRunner();

		Connection conn = DBConnector.getInstance().getMetaConnection();

		ResultSetHandler<JSONArray> handler = new ResultSetHandler<JSONArray>() {

			@Override
			public JSONArray handle(ResultSet rs) throws SQLException {

				JSONArray array = new JSONArray();

				while (rs.next()) {
					int poiNum = rs.getInt("POI_NUM");
					
					array.add(poiNum);
				}
				return array;
			}
		};

		return run.query(conn, sql, handler);
	}
}
