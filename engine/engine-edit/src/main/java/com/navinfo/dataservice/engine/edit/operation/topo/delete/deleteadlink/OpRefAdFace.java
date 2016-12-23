package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
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
        AdAdminSelector selector = new AdAdminSelector(conn);
        for (AdFace adFace : command.getFaces()) {
            result.insertObject(adFace, ObjStatus.DELETE, adFace.getPid());
            AdAdmin admin = (AdAdmin) selector.loadById(adFace.getRegionId(), true);
            if(null != admin){
                if (null != admin && (admin.getAdminType() == 0 || admin.getAdminType() == 1 || admin.getAdminType() == 2 || admin.getAdminType() == 2.5 || admin.getAdminType() == 3 || admin.getAdminType() == 3.5 || admin.getAdminType() == 4 || admin.getAdminType() == 4.5 || admin.getAdminType() == 4.8 || admin.getAdminType() == 5 || admin.getAdminType() == 6 || admin.getAdminType() == 7))
                    AdminIDBatchUtils.updateAdminID(adFace, null, conn, result);
            }
        }
        return null;
    }
}
