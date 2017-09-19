package com.navinfo.dataservice.engine.limit.commons.database.oracle;

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.PoolableConnection;
import org.apache.commons.pool.ObjectPool;

import java.sql.Connection;

public class MyPoolableConnection extends PoolableConnection{
	
	private long sid;
	private long serial;//serial#
	
	
	
	

	public long getSid() {
		return sid;
	}

	public void setSid(long sid) {
		this.sid = sid;
	}

	public long getSerial() {
		return serial;
	}

	public void setSerial(long serial) {
		this.serial = serial;
	}

	public MyPoolableConnection(Connection conn, ObjectPool pool) {
		super(conn, pool);
	}
	
	 /**
    *
    * @param conn my underlying connection
    * @param pool the pool to which I should return when closed
    * @param config the abandoned configuration settings
    */
   public MyPoolableConnection(Connection conn, ObjectPool pool, AbandonedConfig config) {
       super(conn, pool,config);
   }

}
