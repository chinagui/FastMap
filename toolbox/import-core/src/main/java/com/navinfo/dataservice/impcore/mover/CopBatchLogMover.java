package com.navinfo.dataservice.impcore.mover;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

/** 
* @ClassName: DefaultLogMover 
* @author Xiao Xiaowen 
* @date 2016年6月23日 下午4:21:33 
* @Description: TODO
*  
*/
public class CopBatchLogMover extends DefaultLogMover {
	Logger log = LoggerRepos.getLogger(this.getClass());
	public CopBatchLogMover(OracleSchema logSchema, OracleSchema tarSchema,String tempTable,String tempFailLogTable) {
		super(logSchema, tarSchema, tempTable, tempFailLogTable);
	}
	
	@Override
	protected String actionSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("insert into log_action@");
		sb.append(dbLinkName);
		sb.append(" (ACT_ID,US_ID,OP_CMD,STK_ID) select la.act_id,la.us_id,la.op_cmd,la.stk_id from log_action la where la.act_id in (select distinct lp.act_id from log_operation lp where lp.op_id in (select op_id from ");
		sb.append(tempTable);
		sb.append(" t))");
//		if(StringUtils.isNotEmpty(tempFailLogTable)){
//			sb.append(" WHERE NOT EXISTS(SELECT 1 FROM ");
//			sb.append(tempFailLogTable);
//			sb.append(" f WHERE f.row_id=l.row_Id)");
//		}
		return sb.toString();
	}
}
