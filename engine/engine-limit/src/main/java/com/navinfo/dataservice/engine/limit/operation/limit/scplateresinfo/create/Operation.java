package com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.create;

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

        for (int i = 0; i < this.command.getInfos().size(); i++) {

            JSONObject infoObj = this.command.getInfos().getJSONObject(i);

            ScPlateresInfo info = new ScPlateresInfo();

            info.setInfoIntelId(infoObj.getString("infoIntelId"));
            info.setInfoCode(infoObj.getString("infoCode"));
            info.setAdminCode(infoObj.getString("adminCode"));
            info.setUrl(infoObj.getString("url"));
            info.setNewsTime(infoObj.getString("newsTime"));
            info.setInfoContent(infoObj.getString("infoContent"));

            if (infoObj.containsKey("condition")) {
                info.setCondition(infoObj.getString("condition"));
            }

            if (infoObj.containsKey("complete")) {
                info.setComplete(infoObj.getInt("complete"));
            }

            if (infoObj.containsKey("memo")) {
                info.setMemo(infoObj.getString("memo"));
            }

            result.insertObject(info, ObjStatus.INSERT, info.getInfoIntelId());
        }
        return null;
    }
}
