package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;

public class Operation implements IOperation {

    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {

        delGroup(result);

        return null;
    }

    private void delGroup(Result result) {

        for (ScPlateresGeometry geometry:command.getGeometrys()) {

            result.insertObject(geometry, ObjStatus.DELETE, geometry.getGroupId());
        }

    }
}
