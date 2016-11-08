package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.create;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
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
        RdHgwgLimit hgwgLimit = new RdHgwgLimit();
        hgwgLimit.setPid(PidUtil.getInstance().applyRdHgwgLimitPid());
        hgwgLimit.setLinkPid(content.getInt("linkPid"));
        hgwgLimit.setDirect(content.getInt("direct"));
        hgwgLimit.setResHigh(content.getDouble("resHigh"));
        hgwgLimit.setResWeigh(content.getDouble("resWeigh"));
        hgwgLimit.setResAxleLoad(content.getDouble("resAxleLoad"));
        hgwgLimit.setResWidth(content.getDouble("resWidth"));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{content.getDouble("longitude"), content.getDouble("latitude")});
        hgwgLimit.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
        String[] meshes = MeshUtils.point2Meshes(content.getDouble("longitude"), content.getDouble("latitude"));
        hgwgLimit.setMesh(Integer.valueOf(meshes[0]));
        result.insertObject(hgwgLimit, ObjStatus.INSERT, hgwgLimit.pid());
        return null;
    }
}