package com.navinfo.dataservice.engine.limit.operation;


import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.operator.BasicOperator;

import java.sql.Connection;


public class OperatorFactory {

    /**
     * 操作结果写入数据库
     *
     * @param conn   数据库连接
     * @param result 操作结果
     * @throws Exception
     */
    public static void recordData(Connection conn, Result result) throws Exception {

        for (IRow obj : result.getAddObjects()) {
            getOperator(conn, obj).insertRow();
        }

        for (IRow obj : result.getUpdateObjects()) {
            getOperator(conn, obj).updateRow();
        }
        for (IRow obj : result.getDelObjects()) {
            getOperator(conn, obj).deleteRow();
        }
    }

    /**
     * 根据对象，返回操作类
     *
     * @param conn 数据库连接
     * @param obj  对象
     * @return
     * @throws Exception
     */
    private static IOperator getOperator(Connection conn, IRow obj) throws Exception {
        return new BasicOperator(conn, obj);
    }
}
