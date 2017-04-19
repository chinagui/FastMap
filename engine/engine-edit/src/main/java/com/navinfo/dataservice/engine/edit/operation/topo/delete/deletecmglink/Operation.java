package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmglink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.CmgfaceUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmglink
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public class Operation implements IOperation {

    /**
     * 参数
     */
    private Command command;

    /**
     * 数据库链接
     */
    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    /**
     * 执行操作
     *
     * @param result 操作结果
     * @return 操作后的对象
     * @throws Exception
     */
    @Override
    public String run(Result result) throws Exception {
        // 处理CMG-NODE
        List<Integer> excludeCmgnode = new ArrayList<>();
        for (CmgBuildnode cmgnode : command.getCmgnodes()) {
            excludeCmgnode.add(cmgnode.pid());
            result.insertObject(cmgnode, ObjStatus.DELETE, cmgnode.pid());
        }
        // 处理CMG-LINk
        List<Integer> excludeCmglink = new ArrayList<>();
        excludeCmglink.add(command.getCmglink().pid());
        result.insertObject(command.getCmglink(), ObjStatus.DELETE, command.getCmglink().pid());
        // 处理CMG-FACE
        CmgfaceUtil.handleCmgface(command.getCmgfaces(), result, excludeCmgnode, excludeCmglink, conn);
        return null;
    }


}
