package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
public class Operation implements IOperation {
    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        result.insertObject(command.getMileagepile(), ObjStatus.DELETE, command.getObjId());
        return null;
    }
}
