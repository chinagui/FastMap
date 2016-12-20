package com.navinfo.dataservice.engine.edit.operation.obj.adface.delete;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand implements ICommand {

    private String requester;

    private int faceId;

    private AdFace face;

    private AdAdmin adAdmin;

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public AdFace getFace() {
        return face;
    }

    public void setFace(AdFace face) {
        this.face = face;
    }

    public AdAdmin getAdAdmin() {
        return adAdmin;
    }

    public void setAdAdmin(AdAdmin adAdmin) {
        this.adAdmin = adAdmin;
    }

    @Override
    public OperType getOperType() {
        return OperType.DELETE;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.ADFACE;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    public Command(JSONObject json, String requester) throws Exception {
        this.requester = requester;
        this.faceId = json.getInt("objId");
        this.setDbId(json.getInt("dbId"));
    }

}
