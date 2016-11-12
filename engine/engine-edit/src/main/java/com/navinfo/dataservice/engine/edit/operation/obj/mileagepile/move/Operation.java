package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import net.sf.json.JSONObject;

/**
 * Created by chaixin on 2016/11/9 0009.
 */
public class Operation implements IOperation {
    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{command.getContent().getDouble("longitude"), command.getContent().getDouble("latitude")});
        command.getMileagepile().setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
        String[] meshes = MeshUtils.point2Meshes(command.getContent().getDouble("longitude"), command.getContent().getDouble("latitude"));
        command.getMileagepile().setMeshId(Integer.valueOf(meshes[0]));
        result.insertObject(command.getMileagepile(), ObjStatus.UPDATE, command.getMileagepile().pid());
        return null;
    }
}
