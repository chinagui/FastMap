package com.navinfo.dataservice.scripts;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.deepinfo.DeepInfoImporter;

public class InitFmgdbFromHadoopScriptsInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

//			String path = args[0];

			String path = "c:/out.txt";
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "dubbo-consumer-4scripts.xml" });
			context.start();
			new ApplicationContextUtil().setApplicationContext(context);

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