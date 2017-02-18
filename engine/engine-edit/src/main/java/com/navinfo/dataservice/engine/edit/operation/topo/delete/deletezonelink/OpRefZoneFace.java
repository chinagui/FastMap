package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;

public class OpRefZoneFace implements IOperation {

    private Command command;

    private Connection conn;

    public OpRefZoneFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        AdAdminSelector selector = new AdAdminSelector(conn);
        for (ZoneFace zoneFace : command.getFaces()) {
            result.insertObject(zoneFace, ObjStatus.DELETE, zoneFace.getPid());
            if (zoneFace.getRegionId() == 0)
                continue;
            AdAdmin admin = (AdAdmin) selector.loadById(zoneFace.getRegionId(), true);
            if (null != admin && (admin.getAdminType() == 8 || admin.getAdminType() == 9)) {
                ZoneIDBatchUtils.updateZoneID(zoneFace, null, conn, result);
            }
        }
        return null;
    }
}
