package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;

/**
 * Created by chaixin on 2016/11/8 0008.
 */
public class Operation implements IOperation {

    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        RdHgwgLimit hgwgLimit = command.getHgwgLimit();
        boolean isChanged = hgwgLimit.fillChangeFields(command.getContent());
        if (isChanged) {
            result.insertObject(hgwgLimit, ObjStatus.UPDATE, hgwgLimit.pid());
        }
        return null;
    }
}
