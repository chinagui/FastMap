package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode;


import com.navinfo.dataservice.engine.edit.utils.batch.UrbanBatchUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;

import java.sql.Connection;


public class OpRefLuFace implements IOperation {
    protected Logger log = Logger.getLogger(this.getClass());
    private Command command;

    private Connection conn;

    public OpRefLuFace(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        String msg = null;
        // 删除土地利用点对应的面关系
        for (LuFace face : command.getFaces()) {
            result.insertObject(face, ObjStatus.DELETE, face.pid());
            if(face.getKind() == 21)
                UrbanBatchUtils.updateUrban(face.getGeometry(), null, conn, result);
            result.setPrimaryPid(face.getPid());
        }
        return msg;
    }
}

	

