package com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresInfo;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import net.sf.json.JSONObject;


public class Command extends AbstractCommand {
    private String requester;

    private String infoIntelId = "";

    private JSONObject content;

    ScPlateresInfo info;

    public String getInfoIntelId() {
        return infoIntelId;
    }

    public JSONObject getContent() {
        return content;
    }

    public ScPlateresInfo getInfo() {
        return info;
    }

    public void setInfo(ScPlateresInfo info) {
        this.info = info;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

        this.infoIntelId = json.getString("infoIntelId");

        this.content = json.getJSONObject("data");
    }

    @Override
    public OperType getOperType() {
        return OperType.UPDATE;
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
        return LimitObjType.SCPLATERESINFO;
    }
}
