package com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * 删除ZONE面基础参数类
 *
 * @author zhaokk
 */
public class Command extends AbstractCommand implements ICommand {

    private String requester;

    private int faceId;

    private AdAdmin adAdmin;

    private ZoneFace zoneFace;

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public AdAdmin getAdAdmin() {
        return adAdmin;
    }

    public ZoneFace getZoneFace() {
        return zoneFace;
    }

    public void setZoneFace(ZoneFace zoneFace) {
        this.zoneFace = zoneFace;
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
        return ObjType.ZONEFACE;
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
