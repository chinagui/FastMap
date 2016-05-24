package com.navinfo.dataservice.engine.check.meta;

import java.sql.Connection;
import com.navinfo.dataservice.engine.dao.DBConnector;

public class BaseMetaLoader{
	private static Connection metaConn;

	public BaseMetaLoader() {
		// TODO Auto-generated constructor stub
	}

	public static Connection getMetaConn() throws Exception {
		metaConn=DBConnector.getInstance().getMetaConnection();
		return metaConn;
	}

	public static void closeMetaConn() {
		if (metaConn != null) {
			try {
				metaConn.close();
			} catch (Exception e) {

			}
		}
	}

}
