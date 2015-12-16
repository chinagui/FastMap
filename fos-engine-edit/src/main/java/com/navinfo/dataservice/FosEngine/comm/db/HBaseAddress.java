package com.navinfo.dataservice.FosEngine.comm.db;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.log4j.Logger;
import org.hbase.async.HBaseClient;

import com.navinfo.dataservice.FosEngine.comm.constant.PropConstant;

/**
 * HBase连接池
 */
public class HBaseAddress {

	private static Logger logger = Logger.getLogger(HBaseAddress.class);

	private static Connection conn;

	private static HBaseClient client = null;

	/**
	 * 根据配置文件初始化连接池
	 * 
	 * @throws IOException
	 */
	public static void initHBaseAddress() throws IOException {

		Configuration conf = new Configuration();

		String address = ConfigLoader.getConfig().getString(PropConstant.hbaseQuorum);

		conf.set("hbase.zookeeper.quorum", address);

		conn = ConnectionFactory.createConnection(conf);

	}

	/**
	 * 根据输入的地址初始化连接
	 * 
	 * @param address
	 *            连接地址
	 * @throws IOException
	 */
	public static void initHBaseAddress(String address) throws IOException {

		logger.info("初始化HBase连接");

		Configuration conf = new Configuration();

		conf.set("hbase.zookeeper.quorum", address);

		conn = ConnectionFactory.createConnection(conf);

		logger.info("初始化HBase连接完成");

	}

	/**
	 * 获取HTable
	 * 
	 * @param tabName
	 *            表名
	 * @return
	 */
	public static Object getHTable(String tabName) {
		return null;
	}

	/**
	 * 创建HBaseClient
	 * 
	 * @param quorumSpec
	 *            配置信息
	 */
	public static void initHBaseClient(String quorumSpec) {
		client = new HBaseClient(quorumSpec);
	}

	/**
	 * 获取HBaseClient
	 * 
	 * @return
	 */
	public static HBaseClient getHBaseClient() {
		return client;
	}

	/**
	 * 获取HBase连接
	 * 
	 * @return
	 */
	public static Connection getHBaseConnection() {
		return conn;
	}

}
