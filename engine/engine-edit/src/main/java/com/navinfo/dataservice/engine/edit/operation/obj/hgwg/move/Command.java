package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/11/8 0008.
 */
public class Command extends AbstractCommand {

    private String requester;

    private JSONObject content;

    private RdHgwgLimit hgwgLimit;

    public RdHgwgLimit getHgwgLimit() {
        return hgwgLimit;
    }

    public void setHgwgLimit(RdHgwgLimit hgwgLimit) {
        this.hgwgLimit = hgwgLimit;
    }

    public JSONObject getContent() {
        return content;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        this.content = json.getJSONObject("data");
    }

    @Override
    public OperType getOperType() {
        return null;
    }

    @Override
    public String getRequester() {
        return null;
    }

    @Override
    public ObjType getObjType() {
        return null;
    }
}
