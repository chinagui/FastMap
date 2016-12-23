package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode;


import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;

import java.sql.Connection;

/**
 * 删除ZONE点对应删除FACE信息
 *
 * @author zhaokk
 */

public class OpRefAdFace implements IOperation {
    protected Logger log = Logger.getLogger(this.getClass());
    private Command command;

    private Connection conn;

    public OpRefAdFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        AdAdminSelector selector = new AdAdminSelector(conn);
        for (ZoneFace face : command.getFaces()) {
            result.insertObject(face, ObjStatus.DELETE, face.pid());
            AdAdmin admin = (AdAdmin) selector.loadById(face.getRegionId(), true);
            if (null != admin && (admin.getAdminType() == 8 || admin.getAdminType() == 9)) {
                ZoneIDBatchUtils.updateZoneID(face, null, conn, result);
            }
            result.setPrimaryPid(face.getPid());
        }
        return null;
    }
}

	

