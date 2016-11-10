package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.update;

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
        boolean isChanged = command.getMileagepile().fillChangeFields(command.getContent());
        if (isChanged) {
            result.insertObject(command.getMileagepile(), ObjStatus.UPDATE, command.getMileagepile().pid());
        }
        return null;
    }
}
