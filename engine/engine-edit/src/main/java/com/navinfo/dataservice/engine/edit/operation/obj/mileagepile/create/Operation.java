package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.create;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
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
        RdMileagepile mileagepile = new RdMileagepile();
        JSONObject content = command.getContent();
        mileagepile.setPid(PidUtil.getInstance().applyRdMileagepilePid());
        mileagepile.setLinkPid(content.getInt("linkPid"));
        mileagepile.setDirect(content.getInt("direct"));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{content.getDouble("longitude"), content.getDouble("latitude")});
        mileagepile.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
        String[] meshes = MeshUtils.point2Meshes(content.getDouble("longitude"), content.getDouble("latitude"));
        mileagepile.setMeshId(Integer.valueOf(meshes[0]));
        result.insertObject(mileagepile, ObjStatus.INSERT, mileagepile.pid());
        return null;
    }
}
