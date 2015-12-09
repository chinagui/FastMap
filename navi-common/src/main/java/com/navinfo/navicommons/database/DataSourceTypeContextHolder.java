package com.navinfo.navicommons.database;

import org.springframework.util.Assert;
/**
 * 
 * @author liuqing
 *
 */
public class DataSourceTypeContextHolder {

	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

	public static void setDataSourceType(String dataSourceType) {
		Assert.notNull(dataSourceType, "dataSourceType cannot be null");
		contextHolder.set(dataSourceType);
	}

	public static String getDataSourceType() {
		return contextHolder.get();
	}

	public static void clearDataSourceType() {
		contextHolder.remove();
	}
}
