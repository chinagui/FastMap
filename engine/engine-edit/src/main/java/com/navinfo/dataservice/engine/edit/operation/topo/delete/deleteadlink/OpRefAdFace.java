package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.engine.edit.utils.batch.AdminIDBatchUtils;

public class OpRefAdFace implements IOperation {

    private Command command;

    private Connection conn;

    public OpRefAdFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        for (AdFace adFace : command.getFaces()) {
            result.insertObject(adFace, ObjStatus.DELETE, adFace.getPid());
            AdminIDBatchUtils.updateAdminID(adFace, null, adFace.getMeshId(), conn, result);
        }
        return null;
    }
}
