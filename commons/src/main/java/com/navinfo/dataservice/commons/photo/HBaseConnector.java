package com.navinfo.dataservice.commons.photo;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
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
	
	private Connection connection;
	
	public Connection getConnection() throws IOException {
		if (connection == null) {
			synchronized (this) {
				if (connection == null) {
					String hbaseAddress = SystemConfigFactory.getSystemConfig()
							.getValue(PropConstant.hbaseAddress);

					Configuration conf = new Configuration();

					conf.set("hbase.zookeeper.quorum", hbaseAddress);

					connection = ConnectionFactory.createConnection(conf);
				}
			}
		}
		return connection;
	}
}
