package com.navinfo.dataservice.scripts;

import java.sql.Connection;

import org.navinfo.dataservice.meta.PatternImageExporter;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

public class PatternImageExportScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Connection conn = null;

		try {

			String path = args[0];

			conn = MultiDataSourceFactory.getInstance().getMetaDataSource()
					.getConnection();

			PatternImageExporter exporter = new PatternImageExporter(conn);

			exporter.export2Sqlite(path);

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}

	}

}
