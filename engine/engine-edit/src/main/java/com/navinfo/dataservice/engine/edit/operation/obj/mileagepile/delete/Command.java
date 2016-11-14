package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete;

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

    private int objId;

    private RdMileagepile mileagepile;

    public int getObjId() {
        return objId;
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

        this.objId = json.getInt("objId");
    }

    @Override
    public OperType getOperType() {
        return OperType.DELETE;
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
