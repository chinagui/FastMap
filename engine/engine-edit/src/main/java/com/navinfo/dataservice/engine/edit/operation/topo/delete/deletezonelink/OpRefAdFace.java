package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;

public class OpRefAdFace implements IOperation {

    private Command command;

    private Connection conn;

    public OpRefAdFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        for (ZoneFace zoneFace : command.getFaces()) {
            result.insertObject(zoneFace, ObjStatus.DELETE, zoneFace.getPid());
            ZoneIDBatchUtils.updateZoneID(zoneFace, null, zoneFace.getMeshId(), conn, result);
        }
        return null;
    }
}
