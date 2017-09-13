package com.navinfo.dataservice.bizcommons.dao;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: DaoOperator
 * @author xiaoxiaowen4127
 * @date 2017年8月24日
 * @Description: DaoOperator.java
 */
public abstract class DaoOperator {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected Connection conn;
	protected QueryRunner run;
	public DaoOperator(Connection conn){
		this.conn = conn;
		run = new QueryRunner();
	}
	protected void replaceLongString2Clob(Object[] cols)throws Exception{
		if(cols==null||cols.length==0){
			return;
		}
		for(int i=0;i<cols.length;i++){
			Object o = cols[i];
			if(o!=null&&o instanceof String&&((String)o).length()>1000){
				cols[i] = ConnectionUtil.createClob(conn, (String)o);
			}
		}
	}
}
