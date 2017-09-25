package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;

public class Operation implements IOperation {

    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {

        DelGroup(result);

        return null;
    }

    private void DelGroup(Result result) {
        result.insertObject(command.getGroup(), ObjStatus.DELETE, command.getGroup().getGroupId());
    }
}
