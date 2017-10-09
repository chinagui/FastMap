package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Command extends AbstractCommand {

    private String requester;

    private List<String>ids=new ArrayList<>();

    private List<ScPlateresGeometry> geometrys=new ArrayList<>();

    public List<String> getIds() {
        return ids;
    }

    public List<ScPlateresGeometry> getGeometrys() {
        return geometrys;
    }

    public void setGeometrys(List<ScPlateresGeometry> geometrys) {
        this.geometrys = geometrys;
    }

    private boolean isCheckInfect = false;

    public boolean isCheckInfect() {
        return isCheckInfect;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;

        if (json.containsKey("infect") && json.getInt("infect") == 1) {
            this.isCheckInfect = true;
        }

        ids = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("objIds")));
    }

    @Override
    public OperType getOperType() {
        return OperType.DELETE;
    }

    @Override
    public DbType getDbType() {
        return DbType.LIMITDB;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    @Override
    public LimitObjType getObjType() {
        return LimitObjType.SCPLATERESGROUP;
    }

}
