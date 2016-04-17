package com.navinfo.dataservice.commons.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * 进度管理类
 */
public class ProgressService {
	
	private static Logger logger = Logger.getLogger(ProgressService.class);

	private Connection conn;

	private String uuid;

	/**
	 * @param conn
	 *            Oracle连接
	 * @param uuid
	 *            唯一标识
	 * @throws SQLException
	 */
	public ProgressService(Connection conn, String uuid) throws SQLException {
		this.conn = conn;

		this.uuid = uuid;

		insertProgress("0%");
	}

	/**
	 * 进度表中添加一行
	 * 
	 * @param progressInfo
	 *            进度信息
	 * @throws SQLException
	 */
	private void insertProgress(String progressInfo) throws SQLException {
		Statement stmt = conn.createStatement();

		String sql = "insert into task_progress(uuid, progress_info) values('"
				+ uuid + "','" + progressInfo + "')";

		stmt.executeUpdate(sql);
		
		stmt.close();
		
	}

	/**
	 * 更新进度信息
	 * 
	 * @param progressInfo
	 *            进度信息
	 * @throws SQLException
	 */
	public void updateProgress(String progressInfo) throws SQLException {
		Statement stmt = conn.createStatement();

		String sql = "update task_progress set progress_info='" + progressInfo
				+ "' where uuid='" + uuid + "'";

		stmt.executeUpdate(sql);
		
		stmt.close();
		
	}
}
