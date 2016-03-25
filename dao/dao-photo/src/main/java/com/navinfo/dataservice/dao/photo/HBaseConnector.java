package com.navinfo.dataservice.dao.photo;

import org.hbase.async.HBaseClient;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
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
					String hbaseAddress=SystemConfigFactory.getSystemConfig()
							.getValue(PropConstant.hbaseAddress);
					client = new HBaseClient(hbaseAddress);
				}
			}
		}
		return client;
	}

}
