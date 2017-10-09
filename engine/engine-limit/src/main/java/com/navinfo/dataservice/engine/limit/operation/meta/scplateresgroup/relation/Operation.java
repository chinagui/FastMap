package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.relation;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;

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

        for (ScPlateresGroup group : command.getGroups()) {

            if (!group.getInfoIntelId().equals(command.getInfoIntelId())) {

                group.changedFields().put("infoIntelId",command.getInfoIntelId());

                result.insertObject(group, ObjStatus.UPDATE, group.getGroupId());
            }
        }
    }
}
