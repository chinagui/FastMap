package com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresInfo;
import net.sf.json.JSONObject;

public class Operation  implements IOperation {

    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {

        updateGroup(result);

        return null;
    }

    private void updateGroup(Result result) throws Exception {

        JSONObject content = command.getContent();

        ScPlateresInfo info = command.getInfo();

        if (content.containsKey("objStatus") && ObjStatus.UPDATE.toString().equals(content.getString("objStatus"))) {

            boolean isChanged = info.fillChangeFields(content);

            if (isChanged) {
                result.insertObject(info, ObjStatus.UPDATE, info.getInfoIntelId());
            }
        }

        result.setPrimaryId(info.getInfoIntelId());
    }
}
