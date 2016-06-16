package com.navinfo.dataservice.engine.statics.tools;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SystemMessage {

	private SystemMessage() {

	}

	/**
	 * 从配置文件获取参数
	 * 
	 * @param bundle_name
	 * @param key
	 * @return
	 */
	public static String getString(String bundle_name, String key) {

		try {

			return ResourceBundle.getBundle(bundle_name).getString(key);

		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}

	}

}
