package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
public class Command extends AbstractCommand {
    private String requester;

    private RdMileagepile mileagepile;

    private JSONObject content;

    public JSONObject getContent() {
        return content;
    }

    public RdMileagepile getMileagepile() {

        return mileagepile;
    }

    public void setMileagepile(RdMileagepile mileagepile) {
        this.mileagepile = mileagepile;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        this.content = json.getJSONObject("data");
    }

    @Override
    public OperType getOperType() {
        return OperType.MOVE;
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
