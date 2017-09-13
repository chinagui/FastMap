package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;

/**
 * Created by ly on 2017/8/18.
 */
public class Operation implements IOperation {

    private RdLinkWarning rdLinkWarning;

    public Operation(Command command) {

        this.rdLinkWarning = command.getRdLinkWarning();
    }

    @Override
    public String run(Result result) throws Exception {

        return delete(result);
    }

    private String delete(Result result) {

        result.insertObject(rdLinkWarning, ObjStatus.DELETE, rdLinkWarning.getPid());

        return null;
    }
}
