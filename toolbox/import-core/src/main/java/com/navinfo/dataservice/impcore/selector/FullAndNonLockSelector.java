package com.navinfo.dataservice.impcore.selector;

import java.sql.Connection;

import com.navinfo.dataservice.commons.database.OracleSchema;

/** 
 * @ClassName: FullAndNonLockSelector
 * @author xiaoxiaowen4127
 * @date 2016年8月2日
 * @Description: FullAndNonLockSelector.java
 */
public class FullAndNonLockSelector extends LogSelector {

	public FullAndNonLockSelector(OracleSchema logSchema) {
		super(logSchema);
	}

	@Override
	protected int selectLog(Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable);
		sb.append(" SELECT P.OP_ID,P.OP_DT,P.OP_SEQ FROM LOG_OPERATION P");
		return run.update(conn, sb.toString());
	}


	@Override
	protected int extendLog(Connection conn) throws Exception {
		return 0;
	}
	
	@Override
	protected String getOperationLockSql()  {
		return null;
	}

	@Override
	protected String getUnlockLogSql(boolean commitStatus)  {
		return null;
	}

}
