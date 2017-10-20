package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

    private String requester;

    private JSONObject content;

    private String groupId;

    private ScPlateresGroup group;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public ScPlateresGroup getGroup() {
        return group;
    }

    public void setGroup(ScPlateresGroup group) {
        this.group = group;
    }

    JSONObject getContent() {
        return content;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

        this.groupId = json.getString("objId");

        this.content = json.getJSONObject("data");
    }

    @Override
    public OperType getOperType() {
        return OperType.UPDATE;
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
        return LimitObjType.SCPLATERESGROUP;
    }
}
