package com.navinfo.dataservice.dao.photo;

import org.hbase.async.HBaseClient;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.constant.PropConstant;

public class HBaseConnector {

	private static class SingletonHolder {
		private static final HBaseConnector INSTANCE = new HBaseConnector();
	}

	public static final HBaseConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private HBaseClient client;

	public HBaseClient getClient() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					client = new HBaseClient(SystemConfig.getSystemConfig()
							.getValue(PropConstant.hbaseAddress));
				}
			}
		}
		return client;
	}

}
