package com.navinfo.dataservice.dao.pool;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.navinfo.dataservice.datahub.manager.DbManager;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.engine.man.project.ProjectSelector;

/**
 * oracle连接池管理器
 */
public class GlmDbPoolManager {

	private static class SingletonHolder {
		private static final GlmDbPoolManager INSTANCE = new GlmDbPoolManager();
	}

	public static final GlmDbPoolManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 存放各个项目的连接池 key为项目ID value为DataSource
	 */
	private Map<String, DataSource> map = new HashMap<String, DataSource>();

	/**
	 * 通過項目ID獲取數據庫連接
	 * 
	 * @param projectId
	 *            项目ID
	 * @return Oracle连接
	 * @throws Exception
	 */
	public Connection getConnection(int projectId) throws Exception {

		String str = String.valueOf(projectId);

		if (!map.containsKey(str)) {
			synchronized (this) {
				if (!map.containsKey(str)) {

					ProjectSelector selector = new ProjectSelector();

					int dbId = selector.getDbId(projectId);

					DbManager manager = new DbManager();

					OracleSchema db = (OracleSchema) manager.getDbById(dbId);

					map.put(str, db.getPoolDataSource());
				}
			}
		}

		return map.get(str).getConnection();
	}
}