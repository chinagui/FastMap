package com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.edit.utils.batch.AdminIDBatchUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;

public class Operation implements IOperation {
    /**
     * ZONE 面操作类
     */
    private Command command;

    private Check check;

    private Connection conn;

    public Operation(Command command, Check check, Connection conn) {
        this.command = command;

        this.check = check;

        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        ZoneFace zoneFace = command.getZoneFace();
        result.insertObject(zoneFace, ObjStatus.DELETE, zoneFace.getPid());
        relateRegionId(result, zoneFace);
        return null;
    }

    private void relateRegionId(Result result, ZoneFace zoneFace) throws Exception {
        AdAdmin adAdmin = command.getAdAdmin();
        if (null != adAdmin) {
            double type = adAdmin.getAdminType();
            if (type == 8 || type == 9)
                ZoneIDBatchUtils.updateZoneID(zoneFace, null, conn, result);
        }
    }

}
