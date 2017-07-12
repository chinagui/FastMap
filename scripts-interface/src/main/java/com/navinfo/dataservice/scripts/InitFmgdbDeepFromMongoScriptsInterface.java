package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.impcore.deepinfo.DeepInfoImporter;

public class InitFmgdbDeepFromMongoScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			String path = args[0];

			JobScriptsInterface.initContext();

			DeepInfoImporter importer = new DeepInfoImporter();

			importer.run(path, "not_found_pid.txt");

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}

}