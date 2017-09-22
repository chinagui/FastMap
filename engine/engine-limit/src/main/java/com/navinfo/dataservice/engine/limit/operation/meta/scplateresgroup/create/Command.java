package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.create;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

    private String requester;

    private String infoIntelId = "";//情报主键

    private int adAdmin = 0;//行政代码

    private int groupType = 1;//类型

    private String principle = "";//限制规定
    private String condition = "";//限制规定

    public String getInfoIntelId() {
        return infoIntelId;
    }

    public int getAdAdmin() {
        return adAdmin;
    }

    public int getGroupType() {
        return groupType;
    }

    public String getPrinciple() {
        return principle;
    }

    public String getCondition() {
        return condition;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));
        JSONObject data = json.getJSONObject("data");
        this.infoIntelId = data.getString("infoIntelId");
        this.condition = data.getString("condition");
        this.adAdmin = data.getInt("adAdmin");
        this.groupType = data.getInt("groupType");
        this.principle = data.getString("principle");
    }

    @Override
    public OperType getOperType() {
        return OperType.CREATE;
    }

    @Override
    public DbType getDbType() {
        return DbType.LIMITDB;
    }

    @Override
    public String getRequester() {
        return this.requester;
    }

    @Override
    public LimitObjType getObjType() {
        return LimitObjType.SCPLATERESGROUP;
    }
}
