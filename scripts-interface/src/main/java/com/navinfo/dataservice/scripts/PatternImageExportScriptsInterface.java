package com.navinfo.dataservice.scripts;

import org.navinfo.dataservice.engine.meta.patternimage.PatternImageExporter;

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