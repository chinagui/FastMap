package com.navinfo.dataservice.FosEngine.comm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.constant.LoggerConstant;
import com.navinfo.dataservice.FosEngine.comm.constant.PropConstant;
import com.navinfo.dataservice.FosEngine.comm.exception.OracleConnException;

/**
 * oracle连接池管理器
 */
public class DBOraclePoolManager {

	private static final Logger logger = Logger
			.getLogger(DBOraclePoolManager.class);

	/**
	 * 存放各个项目的连接池 key为项目ID value为连接池类
	 */
	private static Map<Integer, DBOraclePool> map = new HashMap<Integer, DBOraclePool>();

	/**
	 * 项目库的配置信息
	 */
	private static String ip;

	private static int port;

	private static String serviceName;

	private static String username;

	private static String password;

	/**
	 * 从项目库读取各个项目的连接信息，并初始化连接
	 * 
	 * @throws Exception
	 */
	public static void initPools() throws Exception {

		JSONObject config = ConfigLoader.getConfig();

		ip = config.getString(PropConstant.pmIp);

		port = config.getInt(PropConstant.pmPort);

		serviceName = config.getString(PropConstant.pmServiceName);

		username = config.getString(PropConstant.pmUsername);

		password = config.getString(PropConstant.pmPassword);

		Class.forName(PropConstant.oracleDriver);

		Connection conn = null;

		Statement stmt = null;

		ResultSet resultSet = null;

		try {

			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":"
					+ port + ":" + serviceName, username, password);

			String sql = "select * from prj_project";

			stmt = conn.createStatement();

			resultSet = stmt.executeQuery(sql);

			while (resultSet.next()) {
				try {
					int projectId = resultSet.getInt("project_id");

					String prjDbConn = resultSet.getString("prj_db_conn");

					JSONObject jsonConnMsg = JSONObject.fromObject(prjDbConn);

					DBOraclePool pool = new DBOraclePool(jsonConnMsg);

					map.put(projectId, pool);
				} catch (Exception e) {

					logger.error(LoggerConstant.errorPmConfig, e);
				}
			}

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
			}

			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * 从项目库读取单个项目的连接信息，并初始化其连接
	 * 
	 * @throws Exception
	 */
	public static void addProjectConn(int projectId) throws Exception {

		JSONObject config = ConfigLoader.getConfig();

		ip = config.getString(PropConstant.pmIp);

		port = config.getInt(PropConstant.pmPort);

		serviceName = config.getString(PropConstant.pmServiceName);

		username = config.getString(PropConstant.pmUsername);

		password = config.getString(PropConstant.pmPassword);

		Class.forName(PropConstant.oracleDriver);

		Connection conn = null;

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":"
					+ port + ":" + serviceName, username, password);

			String sql = "select * from prj_project where project_id =:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, projectId);

			resultSet = pstmt.executeQuery();

			String prjDbConn = resultSet.getString("prj_db_conn");

			JSONObject jsonConnMsg = JSONObject.fromObject(prjDbConn);

			DBOraclePool pool = new DBOraclePool(jsonConnMsg);

			map.put(projectId, pool);

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * 添加项目连接池
	 * 
	 * @param jsonConnMsg
	 *            项目库配置信息
	 * @throws Exception
	 */
	public static void addPool(int projectId, JSONObject jsonConnMsg)
			throws Exception {

		DBOraclePool pool = new DBOraclePool(jsonConnMsg);

		map.put(projectId, pool);
	}

	/**
	 * 刪除項目連接池
	 * 
	 * @param jsonConnMsg
	 *            项目库配置信息
	 * @throws Exception
	 */
	public static void delPool(int projectId) throws Exception {

		map.remove(projectId);
	}

	/**
	 * 通過項目ID獲取數據庫連接
	 * 
	 * @param projectId
	 *            项目ID
	 * @return Oracle连接
	 * @throws Exception
	 */
	public static Connection getConnection(int projectId) throws Exception {

		if (!map.containsKey(projectId)) {
			try {
				addProjectConn(projectId);
			} catch (Exception e) {
				throw new OracleConnException(String.valueOf(projectId));
			}

		}

		return map.get(projectId).getConnection();
	}
}
