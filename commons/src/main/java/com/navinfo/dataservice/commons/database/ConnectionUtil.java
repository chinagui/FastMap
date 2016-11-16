package com.navinfo.dataservice.commons.database;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.proxy.jdbc.ClobProxyImpl;
import com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.vividsolutions.jts.geom.util.GeometryTransformer;

import oracle.sql.CLOB;
import oracle.sql.STRUCT;

/** 
* @ClassName: ConnectionUtil 
* @author Xiao Xiaowen 
* @date 2016年9月7日 上午11:18:25 
* @Description: TODO
*/
public class ConnectionUtil {
	protected static Logger log = LoggerRepos.getLogger(ConnectionUtil.class);
	public static Clob createClob(Connection conn)throws SQLException{
		if(conn==null)return null;
		if(conn instanceof DruidPooledConnection){
			ClobProxyImpl impl = (ClobProxyImpl)conn.createClob();
			return impl.getRawClob();
		}else{
			return conn.createClob();
		}
	}
	
	public static CLOB getClob(Connection conn,ResultSet rs,String columnName)throws SQLException{
		CLOB inforGeo;
		if(conn instanceof DruidPooledConnection){
			ClobProxyImpl clobProxyImpl = (ClobProxyImpl) rs.getClob(columnName);
			inforGeo = (CLOB)clobProxyImpl.getRawClob();
		}else{
			inforGeo = (CLOB) rs.getClob(columnName);
		}
		return inforGeo;
	}
	
	public static Connection getObject(Connection conn)throws SQLException{
		if(conn instanceof DruidPooledConnection){
			ConnectionProxyImpl impl = (ConnectionProxyImpl) ((DruidPooledConnection) conn).getConnection();
			return impl.getRawObject();
		}
		return conn;
	}
}
