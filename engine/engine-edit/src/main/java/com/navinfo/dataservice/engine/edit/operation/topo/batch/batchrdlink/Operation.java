package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink;

import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/11/23 0023.
 */
public class Operation implements IOperation {

    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        JSONObject json = null;
        for (int index = 0; index < command.getLinkPids().size(); index++) {
            json = initBasicJson();

            JSONObject data = JSONObject.fromObject(command.getContent().toString());
            data.put("objStatus", ObjStatus.UPDATE.toString());
            data.put("pid", command.getLinkPids().get(index));
            for (Object dataKey : data.keySet()) {
                Object dataValue = data.get(dataKey);
                if (dataValue instanceof JSONArray) {
                    JSONArray array = (JSONArray) dataValue;
                    for (Object aryObj : array.toArray()) {
                        JSONObject object = (JSONObject) aryObj;
                        String objStatus = object.getString("objStatus");
                        if (ObjStatus.INSERT.toString().equals(objStatus)) continue;
                        String rowId = (String) object.getJSONArray("rowId").get(index);
                        object.element("rowId", rowId);
                    }
                }
            }
            json.put("data", data.toString());
//            com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command command = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command(json, json.toString());
//            com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process(command);
//            process.run();
            Transaction transaction = new Transaction(json.toString());
            transaction.run();
        }

        return null;
    }

    private JSONObject initBasicJson() {
        JSONObject json = new JSONObject();
        json.put("dbId", command.getDbId());
        json.put("command", OperType.UPDATE);
        json.put("type", "RDLINK");
        return json;
    }
}
