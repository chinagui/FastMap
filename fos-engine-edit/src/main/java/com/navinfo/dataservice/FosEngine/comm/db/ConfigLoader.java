package com.navinfo.dataservice.FosEngine.comm.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.constant.LoggerConstant;
import com.navinfo.dataservice.FosEngine.comm.constant.PropConstant;
import com.navinfo.dataservice.FosEngine.comm.service.PidService;

/**
 * 配置信息读取类
 */
public class ConfigLoader {

	private static Logger logger = Logger.getLogger(ConfigLoader.class);

	/**
	 * 配置信息对象
	 */
	private static JSONObject config;

	/**
	 * 从输入的配置文件中读取配置信息
	 * 
	 * @param path
	 *            配置文件路径
	 * @throws IOException
	 */
	public static void loadConfig(String path) throws Exception {
		Properties props = new Properties();

		InputStream in = new FileInputStream(path);

		props.load(in);

		config = JSONObject.fromObject(props);
	}

	/**
	 * 初始化数据库连接
	 * 
	 * @param path
	 *            配置文件路径
	 * @throws IOException
	 */
	public static void initDBConn(String path)  {

		try {
			loadConfig(path);

			// 项目库初始化
			DBOraclePoolManager.initPools();

			// PID库初始化
			PidService.getInstance();

			// hbase初始化
			HBaseAddress.initHBaseClient(config
					.getString(PropConstant.hbaseQuorum));

			// solr初始化
		} catch (Exception e) {
			logger.fatal(LoggerConstant.fatal, e);

			System.exit(0);
		}

	}

	/**
	 * @return 配置信息对象
	 */
	public static JSONObject getConfig() {

		return config;
	}
}
