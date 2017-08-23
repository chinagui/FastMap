package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.create;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;


public class Command extends AbstractCommand implements ICommand {

    private String requester;

    /**
     * RDLink的pid
     */
    private Integer linkPid;

    private Integer direct;


    private double latitude;

    private double longitude;

    public Integer getLinkPid() {
        return linkPid;
    }

    public void setLinkPid(Integer linkPid) {
        this.linkPid = linkPid;
    }

    public Integer getDirect() {
        return direct;
    }

    public void setDirect(Integer direct) {
        this.direct = direct;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public OperType getOperType() {
        return OperType.CREATE;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDLINKWARNING;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

        this.setDbId(json.getInt("dbId"));

        JSONObject data = json.getJSONObject("data");

        this.direct = data.getInt("direct");

        this.linkPid = data.getInt("linkPid");

        this.longitude = data.getDouble("longitude");

        this.latitude = data.getDouble("latitude");
    }

}
