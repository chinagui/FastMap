package com.navinfo.dataservice.dao.fcc;

import java.io.IOException;

import org.hbase.async.HBaseClient;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

public class HBaseConnector {

	private static class SingletonHolder {
		private static final HBaseConnector INSTANCE = new HBaseConnector();
	}

	public static final HBaseConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private HBaseClient client;
	
	private Connection connection;

	public HBaseClient getClient() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					String hbaseAddress = SystemConfigFactory.getSystemConfig()
							.getValue(PropConstant.hbaseAddress);

					client = new HBaseClient(hbaseAddress);
				}
			}
		}
		return client;
	}
	
	public Connection getConnection() throws IOException {
		if (connection == null) {
			synchronized (this) {
				if (connection == null) {
					String hbaseAddress = SystemConfigFactory.getSystemConfig()
							.getValue(PropConstant.hbaseAddress);
					
//					String hbaseAddress = "192.168.3.156";

					Configuration conf = new Configuration();

					conf.set("hbase.zookeeper.quorum", hbaseAddress);
					
					conf.setLong(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, 600000);
					
					connection = ConnectionFactory.createConnection(conf);
				}
			}
		}
		return connection;
	}
	

}
