package com.navinfo.dataservice.engine.editplus.operation;

import java.sql.Connection;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.log.LogWriter;

/** 
 * @ClassName: OperatorRunner
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: OperatorRunner.java
 */
public class OperatorRunner {
	protected static Logger log = LoggerRepos.getLogger(OperatorRunner.class);
	public static JSONObject run(int dbId,AbstractOperation op)throws Exception{
		Connection conn=null;
		try{
			conn = DBConnector.getInstance().getConnectionById(dbId);
			op.setConn(conn);
			op.loadData();
			OperationResult result = op.execute();
			CheckCommand checkCmd=null;
//			String preCheckMsg = preCheck();
//			if (preCheckMsg != null) {
//				throw new Exception(preCheckMsg);
//			}
			op.flush(result);

//			postCheck();

			conn.commit();
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}
}
