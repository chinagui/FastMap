package com.navinfo.dataservice.commons.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.constant.LoggerConstant;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.exception.OracleConnException;

/**
 * oracle连接池管理器
 */
public class DBOraclePoolManager {

	private static final Logger logger = Logger
			.getLogger(DBOraclePoolManager.class);

	/**
	 * 存放各个项目的连接池 key为项目ID value为连接池类
	 */
	private static Map<String, DBOraclePool> map = new HashMap<String, DBOraclePool>();

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

			String sql = "select a.project_id,b.db_user_name,b.db_user_passwd,c.server_ip,c.server_port,c.service_name from project_info a, db_hub b, db_server c where a.db_id=b.db_id and b.server_id=c.server_id";

			stmt = conn.createStatement();

			resultSet = stmt.executeQuery(sql);

			while (resultSet.next()) {
				try {
					int projectId = resultSet.getInt("project_id");

					String ip = resultSet.getString("server_ip");
					
					int port = resultSet.getInt("server_port");
					
					String service = resultSet.getString("service_name");
					
					String user = resultSet.getString("db_user_name");
					
					String password = resultSet.getString("db_user_passwd");

					DBOraclePool pool = new DBOraclePool(ip,port,service,user,password);

					map.put(String.valueOf(projectId), pool);
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

	public static void initMetaPool() throws Exception {
		
		JSONObject config = ConfigLoader.getConfig();

		JSONObject jsonConnMsg = new JSONObject();

		jsonConnMsg.put("ip", config.getString(PropConstant.metaIp));

		jsonConnMsg.put("port", config.getInt(PropConstant.metaPort));

		jsonConnMsg.put("serviceName",
				config.getString(PropConstant.metaServiceName));

		jsonConnMsg.put("username",
				config.getString(PropConstant.metaUsername));

		jsonConnMsg.put("password",
				config.getString(PropConstant.metaPassword));
		
		DBOraclePool pool = new DBOraclePool(jsonConnMsg);
		
		map.put("meta", pool);
	}
	
	public static void initManagePool() throws Exception {
		
		JSONObject config = ConfigLoader.getConfig();

		JSONObject jsonConnMsg = new JSONObject();

		jsonConnMsg.put("ip", config.getString(PropConstant.pmIp));

		jsonConnMsg.put("port", config.getInt(PropConstant.pmPort));

		jsonConnMsg.put("serviceName",
				config.getString(PropConstant.pmServiceName));

		jsonConnMsg.put("username",
				config.getString(PropConstant.pmUsername));

		jsonConnMsg.put("password",
				config.getString(PropConstant.pmPassword));

		DBOraclePool pool = new DBOraclePool(jsonConnMsg);
		
		map.put("man", pool);
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

			String sql = "select a.project_id,b.db_user_name,b.db_user_passwd,c.server_ip,c.server_port,c.service_name from project_info a, db_hub b, db_server c where a.db_id=b.db_id and b.server_id=c.server_id and a.project_id =:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, Integer.valueOf(projectId));

			resultSet = pstmt.executeQuery();

			String ip = resultSet.getString("server_ip");
			
			int port = resultSet.getInt("server_port");
			
			String service = resultSet.getString("service_name");
			
			String user = resultSet.getString("db_user_name");
			
			String password = resultSet.getString("db_user_passwd");

			DBOraclePool pool = new DBOraclePool(ip,port,service,user,password);

			map.put(String.valueOf(projectId), pool);

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

		map.put(String.valueOf(projectId), pool);
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

		String str = String.valueOf(projectId);

		if (!map.containsKey(str)) {
			try {
				addProjectConn(projectId);
			} catch (Exception e) {
				throw new OracleConnException(str);
			}

		}

		return map.get(str).getConnection();
	}

	public static Connection getConnectionByName(String name) throws Exception {

		if (!map.containsKey(name)) {
			throw new OracleConnException(name);
		}

		return map.get(name).getConnection();
	}
}
