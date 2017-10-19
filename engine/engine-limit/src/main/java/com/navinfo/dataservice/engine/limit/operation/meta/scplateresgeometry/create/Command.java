package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.create;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import net.sf.json.JSONObject;


public class Command extends AbstractCommand {

    private String requester;

    private String groupId;

    public String getGroupId() {
        return groupId;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

        JSONObject data = json.getJSONObject("data");

        this.groupId = data.getString("groupId");
    }

    @Override
    public OperType getOperType() {
        return OperType.CREATE;
    }

    @Override
    public DbType getDbType() {
        return DbType.METADB;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    @Override
    public LimitObjType getObjType() {
        return LimitObjType.SCPLATERESGEOMETRY;
    }
}
