package com.navinfo.dataservice.column.job;

import java.sql.Connection;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

/** 
 * @ClassName: PoiColumnEditOperation
 * @author xiaoxiaowen4127
 * @date 2016年11月30日
 * @Description: PoiColumnEditOperation.java
 */
public class PoiColumnEditOperation extends AbstractOperation {

	public PoiColumnEditOperation(Connection conn, OperationResult preResult) {
		super(conn, preResult);
	}

	@Override
	public String getName() {
		return "poiColumnEdit";
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		
	}

}
