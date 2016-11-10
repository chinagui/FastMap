package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/11/8 0008.
 */
public class Operation implements IOperation {

    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        JSONObject content = command.getContent();
        RdHgwgLimit hgwgLimit = command.getHgwgLimit();
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{content.getDouble("longitude"), content.getDouble("latitude")});
        hgwgLimit.changedFields.put("geometry", GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
        String[] meshes = MeshUtils.point2Meshes(content.getDouble("longitude"), content.getDouble("latitude"));
        hgwgLimit.changedFields.put("meshId", Integer.valueOf(meshes[0]));
        hgwgLimit.changedFields.put("linkPid", content.getInt("linkPid"));

        result.insertObject(hgwgLimit, ObjStatus.UPDATE, hgwgLimit.pid());
        return null;
    }
}
