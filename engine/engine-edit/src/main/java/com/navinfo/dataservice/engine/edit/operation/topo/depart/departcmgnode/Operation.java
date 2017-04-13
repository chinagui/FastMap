package com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

import java.sql.Connection;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class Operation implements IOperation {

    /**
     * 参数
     */
    private Command command;

    /**
     * 数据链接
     */
    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        return null;
    }
}
