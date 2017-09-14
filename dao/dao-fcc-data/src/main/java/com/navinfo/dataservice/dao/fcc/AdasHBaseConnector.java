package com.navinfo.dataservice.dao.fcc;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.hbase.async.HBaseClient;

import java.io.IOException;

/**
 * ADAShbase连接
 */
public class AdasHBaseConnector {

	private static class SingletonHolder {
		private static final AdasHBaseConnector INSTANCE = new AdasHBaseConnector();
	}

	public static final AdasHBaseConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private Connection connection;
	
	public Connection getConnection() throws IOException {
		if (connection == null) {
			synchronized (this) {
				if (connection == null) {
					String hbaseAddress = SystemConfigFactory.getSystemConfig()
							.getValue(PropConstant.adasHbaseAddress);

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
