package com.navinfo.dataservice.engine.fcc.tile;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;

public class TileBuilder {

	public static Map<String, Configuration> getDbConnection(Properties props)
			throws Exception {

		Map<String, Configuration> map = new HashMap<String, Configuration>();

		String username = props.getProperty("sys.username");

		String password = props.getProperty("sys.password");

		String serviceName = props.getProperty("sys.service.name");

		String ip = props.getProperty("sys.ip");

		String port = props.getProperty("sys.port");

		Class.forName("oracle.jdbc.driver.OracleDriver");

		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip
				+ ":" + port + ":" + serviceName, username, password);

		Statement stmt = conn.createStatement();

		String sql = "select a.*,b.server_ip,b.server_port from db_hub a, db_server b where a.server_id=b.server_id and a.biz_type='regionRoad'";

		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {

			String dbId = rs.getString("db_id");

			String dbName = rs.getString("db_name");

			String dbUserName = rs.getString("db_user_name");

			String dbUserPasswd = rs.getString("db_user_passwd");

			String serverIp = rs.getString("server_ip");

			String serverPort = rs.getString("server_port");

			Configuration conf = new Configuration();

			conf.set("username", dbUserName);

			conf.set("password", dbUserPasswd);

			conf.set("serviceName", dbName);

			conf.set("ip", serverIp);

			conf.set("port", serverPort);

			conf.set("dbId", dbId);
			
			conf.set("fs.defaultFS", props.getProperty("fs.defaultFS"));

			conf.setBoolean("dfs.permissions", false);

			conf.set("hbase.zookeeper.quorum",
					props.getProperty("hbase.zookeeper.quorum"));
			

			conf.set("minDegree", props.getProperty("min.degree"));

			conf.set("maxDegree", props.getProperty("max.degree"));

			map.put(dbId, conf);

		}

		rs.close();

		stmt.close();

		conn.close();

		return map;
	}

	public static List<Configuration> getConfList(Map<String, Configuration> map, Properties props) throws Exception{

		String username = props.getProperty("man.username");

		String password = props.getProperty("man.password");

		String serviceName = props.getProperty("man.service.name");

		String ip = props.getProperty("man.ip");

		String port = props.getProperty("man.port");

		Class.forName("oracle.jdbc.driver.OracleDriver");

		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip
				+ ":" + port + ":" + serviceName, username, password);

		Statement stmt = conn.createStatement();

		String sql = "select * from region";

		ResultSet rs = stmt.executeQuery(sql);

		List<Configuration> list = new ArrayList<Configuration>();

		while (rs.next()) {

			String dailyDbId = rs.getString("daily_db_id");

			String monthlyDbId = rs.getString("monthly_db_id");

			list.add(map.get(dailyDbId));
			
			list.add(map.get(monthlyDbId));
		}
		
		rs.close();

		stmt.close();

		conn.close();

		return list;
	}

	public static void main(String[] args) throws Exception {

		Properties props = new Properties();

		props.load(new FileInputStream(args[0]));
		
//		props.load(new FileInputStream("C:/FastMap_Road/01Trunk/02代码/05脚本/Sprint5/prjrender_rebuild/conf.properties"));

		Map<String, Configuration> map = getDbConnection(props);

		List<Configuration> list = getConfList(map, props);
		
		for(Configuration conf : list){
			
			AdLinkTileBuilder.run(conf);
			
			RwLinkTileBuilder.run(conf);
			
			RdLinkTileBuilder.run(conf);
		}
	}
}
