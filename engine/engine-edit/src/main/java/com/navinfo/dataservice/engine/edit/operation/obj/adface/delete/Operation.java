package com.navinfo.dataservice.engine.edit.operation.obj.adface.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.engine.edit.utils.batch.AdminIDBatchUtils;

public class Operation implements IOperation {

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
        AdFace adFace = command.getFace();
        result.insertObject(adFace, ObjStatus.DELETE, adFace.getPid());
        relateRegionId(result, adFace);
        return null;
    }

    private void relateRegionId(Result result, AdFace adFace) throws Exception {
        AdAdmin adAdmin = command.getAdAdmin();
        if (null != adAdmin) {
            double type = adAdmin.getAdminType();
            if (type == 0 || type == 1 || type == 2 || type == 2.5 || type == 3 || type == 3.5 || type == 4 || type == 4.5 || type == 4.8 || type == 5 || type == 6 || type == 7 || type == 8)
                AdminIDBatchUtils.updateAdminID(adFace, null, adFace.getMeshId(), conn, result);
        }
    }


}
