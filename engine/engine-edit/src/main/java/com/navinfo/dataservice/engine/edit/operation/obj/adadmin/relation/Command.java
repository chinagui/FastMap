package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.relation;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/11/21 0021.
 */
public class Command extends AbstractCommand {

    private String requester;

    private Integer regionId;

    private Integer facePid;

    private String objectType;

    public Integer getRegionId() {
        return regionId;
    }

    public Integer getFacePid() {
        return facePid;
    }

    public String getObjectType() {
        return objectType;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        setDbId(json.getInt("dbId"));

        JSONObject data = json.getJSONObject("data");
        regionId = data.getInt("regionId");
        facePid = data.getInt("facePid");
        objectType = data.getString("objectType");
    }

    @Override
    public OperType getOperType() {
        return OperType.RELATION;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.ADADMIN;
    }
}
