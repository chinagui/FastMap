package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.relation;

import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/21 0021.
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        IObj face = null;
        if (ObjType.valueOf(command.getObjectType()) == ObjType.ADFACE) {
            AdFaceSelector selector = new AdFaceSelector(conn);
            face = (AdFace) selector.loadById(command.getFacePid(), true);
        } else if (ObjType.valueOf(command.getObjectType()) == ObjType.ZONEFACE) {
            ZoneFaceSelector selector = new ZoneFaceSelector(conn);
            face = (ZoneFace) selector.loadById(command.getFacePid(), true);
        }
        face.changedFields().put("regionId", command.getRegionId());
        result.insertObject(face, ObjStatus.UPDATE, face.pid());
        return null;
    }
}
