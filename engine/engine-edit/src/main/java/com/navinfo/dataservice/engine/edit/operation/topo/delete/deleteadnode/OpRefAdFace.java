package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode;


import com.navinfo.dataservice.engine.edit.utils.batch.AdminIDBatchUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;

import java.sql.Connection;


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
        String msg = null;
        log.debug("删除行政区划点对应的面关系");
        for (AdFace face : command.getFaces()) {
            result.insertObject(face, ObjStatus.DELETE, face.pid());
            AdminIDBatchUtils.updateAdminID(face, null, face.getMeshId(), conn, result);
            result.setPrimaryPid(face.getPid());
        }
        return msg;
    }
}

	

