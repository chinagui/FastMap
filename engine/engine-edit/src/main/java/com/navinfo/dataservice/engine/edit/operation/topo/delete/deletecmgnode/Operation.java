package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmgnode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildnodeSelector;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.CmgfaceUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmgnode
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
        excludeCmgnode.add(command.getCmgnode().pid());
        result.insertObject(command.getCmgnode(), ObjStatus.DELETE, command.getCmgnode().pid());
        // 处理CMG-LINK
        List<Integer> excludeCmglink = new ArrayList<>();
        for (CmgBuildlink cmglink : command.getCmglinks()) {
            excludeCmglink.add(cmglink.pid());
            result.insertObject(cmglink, ObjStatus.DELETE, cmglink.pid());
        }
        // 处理CMG-FACE
        CmgBuildnodeSelector cmgnodeSelector = new CmgBuildnodeSelector(conn);
        CmgBuildlinkSelector cmglinkSelector = new CmgBuildlinkSelector(conn);
        CmgfaceUtil.handleCmgface(command.getCmgfaces(), result, excludeCmgnode, excludeCmglink, cmgnodeSelector, cmglinkSelector);
        return null;
    }

}
