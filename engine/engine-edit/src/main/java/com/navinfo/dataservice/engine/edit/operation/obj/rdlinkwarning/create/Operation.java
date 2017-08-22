package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.create;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import net.sf.json.JSONObject;

import java.sql.Connection;

/**
 * Created by ly on 2017/8/18.
 */
public class Operation implements IOperation {

    private Connection conn;

    private Command command;

    public Operation(Command command, Connection conn) {

        this.command = command;

        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        int meshId = new RdLinkSelector(conn).loadById(command.getLinkPid(), true).mesh();

        RdLinkWarning warning = new RdLinkWarning();

        warning.setMesh(meshId);

        warning.setMeshId(meshId);

        warning.setPid(PidUtil.getInstance().applyRdWarninginfoPid());

        result.setPrimaryPid(warning.getPid());

        JSONObject geoPoint = new JSONObject();

        geoPoint.put("type", "Point");

        geoPoint.put("coordinates", new double[]{command.getLongitude(),
                command.getLatitude()});

        warning.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));

        warning.setDirect(command.getDirect());

        warning.setLinkPid(command.getLinkPid());

        result.insertObject(warning, ObjStatus.INSERT, warning.pid());

        return null;
    }
}
