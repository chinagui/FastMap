package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.create;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class Command extends AbstractCommand {

    private String requester;

    private JSONArray geometrys;

    public JSONArray getGeometrys() {
        return geometrys;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

         geometrys = json.getJSONArray("data");
    }

    @Override
    public OperType getOperType() {
        return OperType.CREATE;
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
        return LimitObjType.SCPLATERESGEOMETRY;
    }
}
