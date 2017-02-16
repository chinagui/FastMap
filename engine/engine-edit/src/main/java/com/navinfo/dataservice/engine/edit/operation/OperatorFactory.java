package com.navinfo.dataservice.engine.edit.operation;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

/**
 * 操作类工厂
 */
public class OperatorFactory {

	/**
	 * 操作结果写入数据库
	 * 
	 * @param conn
	 *            数据库连接
	 * @param result
	 *            操作结果
	 * @throws Exception
	 */
	public static void recordData(Connection conn, Result result) throws Exception {
		
		Set<String> delRowIds = new HashSet<String>();

		for (IRow obj : result.getDelObjects()) {

			getOperator(conn, obj).deleteRow();

			delRowIds.add(obj.rowId());
		}

		for (IRow obj : result.getAddObjects()) {

			getOperator(conn, obj).insertRow();
		}

		for (IRow obj : result.getUpdateObjects()) {

			if (delRowIds.contains(obj.rowId())) {
				continue;
			}
			getOperator(conn, obj).updateRow();
		}
	}

	/**
	 * 根据对象，返回操作类
	 * 
	 * @param conn
	 *            数据库连接
	 * @param obj
	 *            对象
	 * @return
	 * @throws Exception
	 */
	private static IOperator getOperator(Connection conn, IRow obj) throws Exception {
		return new BasicOperator(conn, obj);
	}
}
