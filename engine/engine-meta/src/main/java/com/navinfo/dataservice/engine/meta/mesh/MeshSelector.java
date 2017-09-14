package com.navinfo.dataservice.engine.meta.mesh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class MeshSelector {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	public JSONObject getProvinceByLocation(double lon, double lat)
			throws Exception {

		String meshId = MeshUtils.point2Meshes(lon, lat)[0];

		String sql = "select admincode,province from cp_meshlist where mesh = :1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;

		try {

			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, meshId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				int admincode = resultSet.getInt("admincode");
				
				String province = resultSet.getString("province");

				JSONObject json = new JSONObject();
				
				json.put("id", admincode/10000);
				
				json.put("name", province);

				return json;
			}
		} catch (Exception e) {

			throw new Exception(e);

		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

		return null;
	}
	
	public int getAdminIdByLocation(double lon, double lat)
			throws ServiceException {

		Connection conn = null;
		
		try{
			String meshId = MeshUtils.point2Meshes(lon, lat)[0];

			String selectSql = "select admincode from cp_meshlist where mesh = :1";

			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getMetaConnection();
			
			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						return rs.getInt("admincode");
					}
					return 0;
				}

			};
			
			int adminId = run.query(conn, selectSql, rsHandler, meshId);
			
			if(adminId == 0){
				throw new ServiceException("未找到对应的省市");
			}

			return adminId;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}
	
	/**
	 * @Description:通过图幅号获取行政区划
	 * @param meshId
	 * @return
	 * @throws ServiceException
	 * @author: y
	 * @time:2016-6-28 下午1:53:19
	 */
	public List<Integer> getAdminIdByMesh(String meshId)
			throws ServiceException {

		Connection conn = null;
		
		try{
			String selectSql = "select admincode from cp_meshlist where mesh = :1";

			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getMetaConnection();
			
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>() {
				public List<Integer> handle(ResultSet rs) throws SQLException {
                    List<Integer> rsList = new ArrayList<>();
					while (rs.next()) {
                        rsList.add(rs.getInt("admincode"));
					}
                    return rsList;
				}
			};

            List<Integer> rsList = run.query(conn, selectSql, rsHandler, meshId);
			
			if(rsList == null || rsList.size() == 0){
				throw new ServiceException("未找到对应的省市");
			}

			return rsList;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}
	
	
	
	/**
	 * @Description:通过图幅号获取地级市
	 * @param meshId
	 * @return
	 * @throws ServiceException
	 * @author: y
	 * @time:2016-6-28 下午1:53:19
	 */
	public List<String> getCityListByMesh(String meshId)
			throws ServiceException {

		Connection conn = null;
		
		try{
			String selectSql = "SELECT city FROM sc_partition_meshlist WHERE mesh = :1";

			QueryRunner run = new QueryRunner();

			conn = DBConnector.getInstance().getMetaConnection();
			
			ResultSetHandler<List<String>> rsHandler = new ResultSetHandler<List<String>>() {
				public List<String> handle(ResultSet rs) throws SQLException {
                    List<String> rsList = new ArrayList<String>();
					while (rs.next()) {
                        rsList.add(rs.getString("city"));
					}
                    return rsList;
				}
			};

            List<String> rsList = run.query(conn, selectSql, rsHandler, meshId);

			return rsList;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}

	public static void main(String[] args) throws Exception {

		MeshSelector selector = new MeshSelector();

		System.out.println(selector.getProvinceByLocation(115.57763, 39.92789));
	}
}
