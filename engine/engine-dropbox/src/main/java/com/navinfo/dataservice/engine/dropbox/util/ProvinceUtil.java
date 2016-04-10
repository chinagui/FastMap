package com.navinfo.dataservice.engine.dropbox.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class ProvinceUtil {

	private final String defaultConfigFile = "/com/navinfo/dataservice/engine/dropbox/config/province.properties";

	private static class SingletonHolder {
		private static final ProvinceUtil INSTANCE = new ProvinceUtil();
	}

	public static final ProvinceUtil getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static TreeMap<String, String> provinceMap = null;

	public void init(String configFile) throws Exception {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(configFile);
			if (is == null) {
				is = ProvinceUtil.class.getResourceAsStream(configFile);
			}

			InputStreamReader reader = new InputStreamReader(is);

			provinceMap = new TreeMap<String, String>();

			Properties props = new Properties();

			props.load(reader);

			Set keyValue = props.keySet();

			for (Iterator it = keyValue.iterator(); it.hasNext();) {
				String key = (String) it.next();

				String value = props.getProperty(key);

				provinceMap.put(key, value);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (is != null)
				is.close();
		}

	}

	public String getProvinceName(String proviceId) throws Exception {
		if (provinceMap == null) {
			synchronized (this) {
				if (provinceMap == null) {
					init(defaultConfigFile);
				}
			}
		}

		return provinceMap.get(proviceId);
	}

	public TreeMap<String, String> getProvinceMap() throws Exception {
		if (provinceMap == null) {
			synchronized (this) {
				if (provinceMap == null) {
					init(defaultConfigFile);
				}
			}
		}
		return provinceMap;
	}
}
