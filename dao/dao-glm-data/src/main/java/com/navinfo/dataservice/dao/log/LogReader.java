package com.navinfo.dataservice.dao.log;

import java.sql.Connection;
import java.util.List;

import net.sf.json.JSONObject;

/**
 * 日志查询类
 */
public class LogReader {

	private Connection conn;

	public LogReader(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 根据条件查询
	 * @param jsonCondition
	 * @return
	 */
	public List<LogOperation> queryLog(JSONObject jsonCondition) {
		return null;
	}
}
