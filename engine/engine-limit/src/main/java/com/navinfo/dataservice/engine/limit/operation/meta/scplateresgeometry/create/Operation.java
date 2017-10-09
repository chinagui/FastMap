package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.create;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.sql.Connection;

public class Operation implements IOperation {
    private Command command;
    private Connection conn;

    public Operation(Command command,	 Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        for (int i = 0; i < this.command.getGeometrys().size(); i++) {

            JSONObject obj = this.command.getGeometrys().getJSONObject(i);

            ScPlateresGeometry geometry = new ScPlateresGeometry();

            String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(obj.getString("groupId"),
                    LimitObjType.SCPLATERESGEOMETRY, i);

            geometry.setGeometryId(geomId);

            geometry.setGroupId(obj.getString("groupId"));

            Geometry geom = GeoTranslator.geojson2Jts(obj.getJSONObject("geometry"), 1, 5);

            geometry.setGeometry(geom);

            if (obj.containsKey("boundarylink")) {
                geometry.setBoundarylink(obj.getString("boundarylink"));
            }

            result.insertObject(geometry, ObjStatus.INSERT, geometry.getGeometryId());
        }
        return null;
    }
}
