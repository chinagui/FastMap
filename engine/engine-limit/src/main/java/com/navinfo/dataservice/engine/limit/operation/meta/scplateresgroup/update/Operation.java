package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

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

        ScPlateresGroup group = command.getGroup();

        if (content.containsKey("objStatus") && ObjStatus.UPDATE.toString().equals(
                content.getString("objStatus"))) {

            boolean isChanged = group.fillChangeFields(content);

            if (isChanged) {
                result.insertObject(group, ObjStatus.UPDATE, group.getGroupId());
            }

        }
        result.setPrimaryId(group.getGroupId());
    }
}
