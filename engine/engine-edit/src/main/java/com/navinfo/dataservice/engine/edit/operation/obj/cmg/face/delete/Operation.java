package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.CmglinkUtil;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete
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

    public Operation(Command command) {
        this.command = command;
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
        // 处理CMG-FACE
        result.insertObject(command.getCmgface(), ObjStatus.DELETE, command.getCmgface().pid());
        // 处理CMG-LINK
        CmglinkUtil.handleCmglinkMesh(command.getCmglinks(), command.getCmgface().getMeshId(), result);
        // 处理CMG-NODE
        CmgnodeUtil.handleCmgnodeMesh(command.getCmgnodes(), command.getCmgface().getMeshId(), result);
        return null;
    }

}
