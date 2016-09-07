package com.navinfo.dataservice.bizcommons;

import java.sql.Clob;
import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: MultiDataSourceFactoryTest
 * @author xiaoxiaowen4127
 * @date 2016年9月7日
 * @Description: MultiDataSourceFactoryTest.java
 */
public class MultiDataSourceFactoryTest extends ClassPathXmlAppContextInit{
	@Before
	public void before(){
		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
	}
	@Test
	public void getConnClass(){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			Clob c = conn.createClob();
			System.out.println(conn.getClass().getName());
			System.out.println(c.getClass().getName());
			//druid:com.alibaba.druid.pool.DruidPooledConnection,com.alibaba.druid.proxy.jdbc.ClobProxyImpl
			//dbcp:..
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	
	@Test
	public void createClob_01(){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			Clob c = ConnectionUtil.createClob(conn);
			c.setString(1, "POLYGON((115.20657 39.38821,117.72713 39.3882,117.72713 41.15306,115.20657 41.15306,115.20657 39.38821))");
			String sql = "INSERT INTO TEMP_001 VALUES (1,sdo_geometry(?,8307))";
			new QueryRunner().update(conn, sql, c);
			conn.commit();
			//druid:com.alibaba.druid.pool.DruidPooledConnection,com.alibaba.druid.proxy.jdbc.ClobProxyImpl
			//dbcp:..
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
