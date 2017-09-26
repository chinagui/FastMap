package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.delete;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

    private String requester;
    private String groupId;
    private ScPlateresGroup group;

    public String getGroupId() {
        return groupId;
    }

    public ScPlateresGroup getGroup() {
        return group;
    }

    public void setGroup(ScPlateresGroup group) {
        this.group = group;
    }

    private boolean isCheckInfect = false;

    public boolean isCheckInfect() {
        return isCheckInfect;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;

        this.groupId = json.getString("objId");

        if (json.containsKey("infect") && json.getInt("infect") == 1) {
            this.isCheckInfect = true;
        }
    }

    @Override
    public OperType getOperType() {
        return OperType.DELETE;
    }

    @Override
    public DbType getDbType() {
        return DbType.LIMITDB;
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
