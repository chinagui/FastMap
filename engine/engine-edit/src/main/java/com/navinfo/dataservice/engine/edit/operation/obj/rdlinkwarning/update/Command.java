package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * Created by ly on 2017/8/18.
 */
public class Command extends AbstractCommand {

    private String requester;

    private JSONObject content;

    private int pid;

    private RdLinkWarning rdLinkWarning;

    public RdLinkWarning getRdLinkWarning() {
        return rdLinkWarning;
    }

    public void setRdLinkWarning(RdLinkWarning rdLinkWarning) {
        this.rdLinkWarning = rdLinkWarning;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public JSONObject getContent() {
        return content;
    }

    public void setContent(JSONObject content) {
        this.content = content;
    }

    @Override
    public OperType getOperType() {
        return OperType.UPDATE;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDLINKWARNING;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;

        this.setDbId(json.getInt("dbId"));

        this.content = json.getJSONObject("data");

        this.pid = this.content.getInt("pid");

    }

}
