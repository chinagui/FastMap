package com.navinfo.dataservice.scripts;



import com.navinfo.dataservice.impcore.charge.CharingInfoImporter;


public class InitFmgdbcharingFromMongoScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			String path = args[0];


			JobScriptsInterface.initContext();

			CharingInfoImporter importer = new CharingInfoImporter();

			importer.run(path, "not_found_pid.txt");

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}

}