package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.delete;

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

    private int pid;

    private RdLinkWarning rdLinkWarning;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public RdLinkWarning getRdLinkWarning() {
        return rdLinkWarning;
    }

    public void setRdLinkWarning(RdLinkWarning rdLinkWarning) {
        this.rdLinkWarning = rdLinkWarning;
    }

    @Override
    public OperType getOperType() {
        return OperType.DELETE;
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

        this.pid = json.getInt("objId");
    }

}
