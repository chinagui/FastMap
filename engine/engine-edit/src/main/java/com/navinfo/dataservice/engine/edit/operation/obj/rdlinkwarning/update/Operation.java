package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import net.sf.json.JSONObject;

/**
 * Created by ly on 2017/8/18.
 */
public class Operation implements IOperation {

    private Command command;

    private RdLinkWarning rdLinkWarning;

    public Operation(Command command) {

        this.command = command;

        this.rdLinkWarning = command.getRdLinkWarning();
    }

    @Override
    public String run(Result result) throws Exception {

        JSONObject content = command.getContent();

        if (content.containsKey("objStatus")
                && ObjStatus.UPDATE.toString().equals(content.getString("objStatus"))) {

            boolean isChanged = rdLinkWarning.fillChangeFields(content);

            if (isChanged) {

                result.insertObject(rdLinkWarning, ObjStatus.UPDATE, rdLinkWarning.pid());
            }
        }

        result.setPrimaryPid(rdLinkWarning.getPid());

        return null;
    }

}
