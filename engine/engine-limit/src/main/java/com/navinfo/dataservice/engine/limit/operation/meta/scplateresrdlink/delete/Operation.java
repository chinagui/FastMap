package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresRdlinkSearch;

import java.sql.Connection;
import java.util.List;

public class Operation implements IOperation {


    private Connection conn=null;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        return null;
    }


    public void delByGeometryId(String geometryId, Result result) throws Exception {

        if (conn == null) {
            return;
        }

        ScPlateresRdlinkSearch search = new ScPlateresRdlinkSearch(this.conn);

        List<ScPlateresRdLink> links = search.loadByGeometryId(geometryId);

        for (ScPlateresRdLink link : links) {

            result.insertObject(link, ObjStatus.DELETE, String.valueOf(link.getLinkPid()));
        }
    }
}
