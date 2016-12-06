package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    private RdSpeedlimit limit;

    public Operation(Command command, RdSpeedlimit limit) {
        this.command = command;

        this.limit = limit;
    }

    public Operation(Connection conn) {
        this.conn = conn;

    }

    @Override
    public String run(Result result) throws Exception {

        JSONObject content = command.getContent();

        if (content.containsKey("objStatus")) {

            if (ObjStatus.DELETE.toString().equals(
                    content.getString("objStatus"))) {
                result.insertObject(limit, ObjStatus.DELETE, limit.pid());

                return null;
            } else {

                boolean isChanged = limit.fillChangeFields(content);

                if (isChanged) {
                    result.insertObject(limit, ObjStatus.UPDATE, limit.pid());
                }
            }
        }
        result.setPrimaryPid(limit.getPid());
        return null;
    }

    public void upDownLink(RdNode sNode, List<RdLink> targetLinks,
                           Map<Integer, RdLink> leftLinkMapping,
                           Map<Integer, RdLink> rightLinkMapping, Result result)
            throws Exception {

        List<Integer> linkPids = new ArrayList<Integer>();

        linkPids.addAll(leftLinkMapping.keySet());

        RdSpeedlimitSelector speedlimitSelector = new RdSpeedlimitSelector(conn);

        List<RdSpeedlimit> limits = speedlimitSelector
                .loadSpeedlimitByLinkPids(linkPids, true);

        if (limits.size() == 0) {

            return;
        }

        Map<Integer, RdSpeedlimit> limitMap = new HashMap<Integer, RdSpeedlimit>();

        for (RdSpeedlimit limit : limits) {

            limitMap.put(limit.getLinkPid(), limit);
        }

        int inNodePid = sNode.getPid();

        for (RdLink link : targetLinks) {

            if (!limitMap.containsKey(link.getPid())) {

                inNodePid = inNodePid == link.getsNodePid() ? link
                        .geteNodePid() : link.getsNodePid();

                continue;
            }

            RdSpeedlimit limitTemp = limitMap.get(link.getsNodePid());

            if (limitTemp.getDirect() == 2) {

                if (inNodePid == link.getsNodePid()) {

                    updateRdSpeedlimit(link, limit, result);

                } else {

                    updateRdSpeedlimit(link, limit, result);
                }
            }

            if (limitTemp.getDirect() == 3) {

                if (inNodePid == link.geteNodePid()) {

                    updateRdSpeedlimit(link, limit, result);

                } else {

                    updateRdSpeedlimit(link, limit, result);
                }
            }

            inNodePid = inNodePid == link.getsNodePid() ? link.geteNodePid()
                    : link.getsNodePid();
        }
    }

    private void updateRdSpeedlimit(RdLink link, RdSpeedlimit limit,
                                    Result result) throws Exception {

        Coordinate targetPoint = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(limit.getGeometry(), 0.00001, 5).getCoordinate(), GeoTranslator.transform(link.getGeometry(), 0.00001, 5));

        JSONObject geoPoint = new JSONObject();

        geoPoint.put("type", "Point");

        geoPoint.put("coordinates",
                new double[]{targetPoint.x, targetPoint.y});

        Geometry tmpGeo = GeoTranslator.geojson2Jts(geoPoint);

        geoPoint = GeoTranslator.jts2Geojson(tmpGeo, 0.00001, 5);

        limit.changedFields().put("geometry", geoPoint);

        limit.changedFields().put("linkPid", link.getPid());

        result.insertObject(limit, ObjStatus.UPDATE, limit.pid());
    }
}
