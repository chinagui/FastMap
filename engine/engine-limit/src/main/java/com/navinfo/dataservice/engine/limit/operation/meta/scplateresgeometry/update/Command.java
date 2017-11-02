package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.update;

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

    private JSONObject content;

    private String id;

    private String boundaryLink;

    private ScPlateresGeometry geometry;

    private List<String> ids = null;

    public List<String> getIds() {
        return ids;
    }

    public String getId() {
        return id;
    }

    public ScPlateresGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(ScPlateresGeometry geometry) {
        this.geometry = geometry;
    }


    public String getBoundaryLink() {
        return boundaryLink;
    }

    JSONObject getContent() {
        return content;
    }

    private List<ScPlateresGeometry> geometrys = null;

    public List<ScPlateresGeometry> getGeometrys() {
        return geometrys;
    }

    public void setGeometrys(List<ScPlateresGeometry> geometrys) {
        this.geometrys = geometrys;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

        this.content = json.getJSONObject("data");

        if (json.containsKey("objIds")) {

            ids = new ArrayList<>();

            if (this.content.containsKey("boundaryLink")) {

                boundaryLink = this.content.getString("boundaryLink");

                ids = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("objIds")));
            }

            return;
        }

        this.id = json.getString("objId");


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
