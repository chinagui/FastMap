package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

    private String requester;

    private JSONObject content;

    private String id;

    private ScPlateresGeometry geometry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScPlateresGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(ScPlateresGeometry geometry) {
        this.geometry = geometry;
    }

    JSONObject getContent() {
        return content;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

        this.id = json.getString("objId");

        this.content = json.getJSONObject("data");
    }

    @Override
    public OperType getOperType() {
        return OperType.UPDATE;
    }

    @Override
    public DbType getDbType() {
        return DbType.METADB;
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
