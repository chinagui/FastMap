package com.navinfo.dataservice.bizcommons.service;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Title: DbMeshInfoUtil
 * @Package: com.navinfo.dataservice.engine.edit.utils
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 6/22/2017
 * @Version: V1.0
 */
public class DbMeshInfoUtil {
	private Logger logger = Logger.getLogger(DbMeshInfoUtil.class);
	/**
	 * 几何坐标保留精度位数
	 */
	public final static Integer BASE_PRECISION = 5;

	/**
	 * 基本扩大系数
	 */
	public final static Integer BASE_EXPAND = 100000;

	/**
	 * 基本缩小系数
	 */
	public final static double BASE_SHRINK = 0.00001;

	/**
	 * 最短允许LINK长度
	 */
	public final static double MIN_LINK_LENGTH = 2L;

	private DbMeshInfoUtil() {
	}

	private static class SingletonHolder {
		public static Map<String, Integer> DB_MESHES = new DbMeshInfoUtil()
				.loadData();
	}

	/**
	 * 根据几何计算是否对应多个大区库
	 * 
	 * @param geometry
	 * @return
	 */
	public static Set<Integer> calcDbIds(Geometry geometry) {
		Set<Integer> dbIds = new HashSet<>();
		if (null == geometry) {
			return dbIds;
		}

		String[] meshes = MeshUtils.geometry2Mesh(GeoTranslator.transform(
				geometry, BASE_SHRINK, BASE_PRECISION));
		for (String mesh : meshes) {
			for (Map.Entry<String, Integer> entry : SingletonHolder.DB_MESHES
					.entrySet()) {
				if (entry.getKey().equals(mesh)) {
					dbIds.add(entry.getValue());
				}
			}
		}
		return dbIds;
	}

	/**
	 * 根据图幅计算是否对应多个大区库
	 * 
	 * @param geometry
	 * @return
	 */
	public static Set<Integer> calcDbIds(Set<String> meshes) {
		Set<Integer> dbIds = new HashSet<>();

		for (String mesh : meshes) {
			for (Map.Entry<String, Integer> entry : SingletonHolder.DB_MESHES
					.entrySet()) {
				if (entry.getKey().equals(mesh)) {
					dbIds.add(entry.getValue());
				}
			}
		}
		return dbIds;
	}

	/**
	 * 根据wkt扩圈多个大区库
	 * 
	 * @param geometry
	 * @return
	 * @throws Exception
	 */
	public static Set<Integer> calcDbIds(String wkt, int extendCount)
			throws Exception {
		Set<Integer> dbIds = null;
		String[] meshIds = MeshUtils.geometry2Mesh(GeoTranslator
				.wkt2Geometry(wkt));
		Set<String> extendMeshes = null;
		if (meshIds != null && meshIds.length > 0) {
			extendMeshes = MeshUtils.getNeighborMeshSet(
					new HashSet<>(Arrays.asList(meshIds)), extendCount);
		}
		if (extendMeshes != null) {
			dbIds = DbMeshInfoUtil.calcDbIds(extendMeshes);
		}
		return dbIds;
	}

	public synchronized Map<String, Integer> loadData() {
		Map<String, Integer> dbMeshes = new HashMap<>();

		String sql = "select a.daily_db_id as db_id, c.mesh from region a, cp_region_province b,"
				+ " cp_meshlist@metadb_link c where a.region_id = b.region_id and b.admincode = c.admincode and "
				+ "a.daily_db_id is not null order by a.daily_db_id, c.mesh";

		QueryRunner runner = new QueryRunner();
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			dbMeshes = runner.query(conn, sql, new ParseHandler());
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		return dbMeshes;
	}

	private class ParseHandler implements
			ResultSetHandler<Map<String, Integer>> {
		@Override
		public Map<String, Integer> handle(ResultSet rs) throws SQLException {
			rs.setFetchSize(3000);
			Map<String, Integer> map = new HashMap<>();
			while (rs.next()) {
				map.put(rs.getString(2), rs.getInt(1));
			}
			return map;
		}
	}
}
