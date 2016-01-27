package com.navinfo.dataservice.FosEngine.test;

import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import com.navinfo.dataservice.commons.constant.PropConstant;



public class Test6 {

	public static void main(String[] args) throws Exception{
		for(int i=0;i<100;i++){
		
		Properties p = new Properties();
		p.setProperty("driverClassName", PropConstant.oracleDriver);
		p.setProperty("url", "jdbc:oracle:thin:@" + "192.168.4.131" + ":" + 1521 + ":"
				+ "orcl");
		p.setProperty("password", "tiger");// 连接数据库的密码
		p.setProperty("username", "scott");// 连接数据库的用户名
		p.setProperty("maxActive", "150");// 最大连接数
		p.setProperty("minIdle", "10"); // 最大空闲连接
		p.setProperty("maxIdle", "20"); // 最大空闲连接
		p.setProperty("initialSize", "3000");// 超时等待时间以毫秒为单位
		p.setProperty("logAbandoned", "true");

		p.setProperty("removeAbandoned", "true");// 超时时间(以秒数为单位)
		p.setProperty("removeAbandonedTimeout", "1000");
		p.setProperty("maxWait", "1000000");

		p.setProperty("timeBetweenEvictionRunsMillis", "1000000");
		p.setProperty("numTestsPerEvictionRun", "100000");

		p.setProperty("minEvictableIdleTimeMillis", "1000000");

		BasicDataSource dataSource = (BasicDataSource) BasicDataSourceFactory
				.createDataSource(p);// 创建数据源。
		
		System.out.println("------------->>>>"+i);
		
		}
	}

}
