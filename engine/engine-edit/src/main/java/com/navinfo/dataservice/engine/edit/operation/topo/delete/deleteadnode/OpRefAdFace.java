package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode;


import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
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
        AdAdminSelector selector = new AdAdminSelector(conn);
        log.debug("删除行政区划点对应的面关系");
        for (AdFace face : command.getFaces()) {
            result.insertObject(face, ObjStatus.DELETE, face.pid());
            AdAdmin admin = (AdAdmin) selector.loadById(face.getRegionId(), true);
            if(null != admin) {
                if (null != admin && (admin.getAdminType() == 0 || admin.getAdminType() == 1 || admin.getAdminType() == 2 || admin.getAdminType() == 2.5 || admin.getAdminType() == 3 || admin.getAdminType() == 3.5 || admin.getAdminType() == 4 || admin.getAdminType() == 4.5 || admin.getAdminType() == 4.8 || admin.getAdminType() == 5 || admin.getAdminType() == 6 || admin.getAdminType() == 7))
                    AdminIDBatchUtils.updateAdminID(face, null, conn, result);
            }
            result.setPrimaryPid(face.getPid());
        }
        return "";
    }
}

	

