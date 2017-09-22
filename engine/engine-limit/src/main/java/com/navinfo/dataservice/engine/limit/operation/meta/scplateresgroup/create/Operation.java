package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.create;

import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;


public class Operation implements IOperation {
    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {

        ScPlateresGroup group = new ScPlateresGroup();

        group.setAdAdmin(this.command.getAdAdmin());
        group.setGroupId("S1100000001");
        group.setGroupType(this.command.getGroupType());
        group.setPrinciple(this.command.getPrinciple());
        group.setInfoIntelId(this.command.getInfoIntelId());

        result.insertObject(group, ObjStatus.INSERT, 0);
        return null;
    }
}
