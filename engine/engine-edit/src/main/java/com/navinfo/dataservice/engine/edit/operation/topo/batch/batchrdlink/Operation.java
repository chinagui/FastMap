package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink;

import com.navinfo.dataservice.dao.glm.iface.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/23 0023.
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
//        JSONObject json = null;
//        for (int index = 0; index < command.getLinkPids().size(); index++) {
//            json = initBasicJson();
//
//            JSONObject data = JSONObject.fromObject(command.getContent().toString());
//            data.put("objStatus", ObjStatus.UPDATE.toString());
//            data.put("pid", command.getLinkPids().get(index));
//            for (Object dataKey : data.keySet()) {
//                Object dataValue = data.get(dataKey);
//                if (dataValue instanceof JSONArray) {
//                    JSONArray array = (JSONArray) dataValue;
//                    for (Object aryObj : array.toArray()) {
//                        JSONObject object = (JSONObject) aryObj;
//                        String objStatus = object.getString("objStatus");
//                        if (ObjStatus.INSERT.toString().equals(objStatus)) continue;
//                        String rowId = (String) object.getJSONArray("rowId").get(index);
//                        object.element("rowId", rowId);
//                    }
//                }
//            }
//            json.put("data", data.toString());
//            com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command command = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command(json, json.toString());
//            com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process(command, result, conn);
//            process.innerRun();
//            Transaction transaction = new Transaction(json.toString());
//            transaction.run();
//        }

        JSONObject json = initBasicJson();
        com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command command = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command(json, json.toString());
        com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process(command, result, conn);
        process.innerRun();

        return null;
    }

    private JSONObject initBasicJson() {
        JSONObject json = new JSONObject();
        json.put("dbId", command.getDbId());
        json.put("command", OperType.UPDATE);
        json.put("type", "RDLINK");
        json.put("linkPids", command.getLinkPids());
        json.put("data", command.getContent());
        return json;
    }
}
