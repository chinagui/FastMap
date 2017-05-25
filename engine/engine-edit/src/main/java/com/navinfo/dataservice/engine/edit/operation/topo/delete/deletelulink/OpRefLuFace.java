package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.batch.UrbanBatchUtils;

import java.sql.Connection;

public class OpRefLuFace implements IOperation {

    private Command command;

    private Result result;

    private Connection conn;

    public OpRefLuFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        this.result = result;
        if (null != command.getFaces()) {
            for (LuFace face : command.getFaces()) {
                result.insertObject(face, ObjStatus.DELETE, face.getPid());
                if(face.getKind() == 21)
                    UrbanBatchUtils.updateUrban(GeoTranslator.transform(face.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION),
                            null, conn, result);
            }
        }
        return null;
    }

}
