package com.navinfo.dataservice.scripts;

import java.sql.Connection;

import org.navinfo.dataservice.engine.meta.patternimage.PatternImageExporter;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;

public class PatternImageExportScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			String path = args[0];

			PatternImageExporter exporter = new PatternImageExporter();

			exporter.export2Sqlite(path);

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}

}