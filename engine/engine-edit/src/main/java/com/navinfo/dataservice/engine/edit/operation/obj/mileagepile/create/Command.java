package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
public class Command extends AbstractCommand {

    private String requester;

    private JSONObject content;

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
        return OperType.CREATE;
    }

    @Override
    public String getRequester() {
        return this.requester;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDMILEAGEPILE;
    }
}
