package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ly on 2017/5/16.
 */
public class OpRefRelationObj {

    private Connection conn;

    // 被移动的端点nodepid
    List<Integer> moveNodePids = new ArrayList<>();

    public OpRefRelationObj(Connection conn) {

        this.conn = conn;
    }

    private void getDepartNodePids(Command command) {

        moveNodePids = new ArrayList<>();

        if( command.getCatchInfos()==null)
        {
            return;
        }

        for (int i = 0; i < command.getCatchInfos().size(); i++) {

            JSONObject obj = command.getCatchInfos().getJSONObject(i);
            // 移动的端点node
            int nodePid = obj.getInt("nodePid");

            if (!moveNodePids.contains(nodePid)) {

                moveNodePids.add(nodePid);
            }
        }
    }

    public String handleRelationObj(Command command, List<RwLink> newLinks,RwLink updateLink,
                                     Result result) throws Exception {

        getDepartNodePids(command);

        // 处理同一关系
        handleRdSame(command, result, newLinks);

        // 立交关系
        handleRdGsc(command, result, newLinks, updateLink);

        return "";
    }

    /**
     * 同一关系
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdSame(Command command, Result result, List<RwLink> newLinks)
            throws Exception {

        RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(this.conn);

        Map<Integer, RdSameNode> sameNodeMap = new HashMap<>();

        for (int nodePid : moveNodePids) {

            List<RdSameNode> sameNodes = sameNodeSelector
                    .loadSameNodeByNodePids(String.valueOf(nodePid), "RW_NODE",
                            true);

            if (sameNodes.size() == 1) {

                sameNodeMap.put(nodePid, sameNodes.get(0));

            } else if (sameNodes.size() == 2) {
                throw new Exception("Node " + String.valueOf(nodePid)
                        + "是多个同一点的组成node");
            }
        }

        if (sameNodeMap.size() > 1) {

            throw new Exception("link两端点均是同一点关系的组成node，不能同时对两个端点node进行修形操作");
        }

        // 端点坐标不变
        if (moveNodePids.size() == 0) {

            return null;
        }

        Map<Integer, Geometry> nodeGeoMap = new HashMap<>();

        for (int i = 0; i < command.getCatchInfos().size(); i++) {

            JSONObject obj = command.getCatchInfos().getJSONObject(i);
            // 分离移动的node
            int nodePid = obj.getInt("nodePid");

            for (RwLink link : newLinks) {

                LineString linkGeo = (LineString) link.getGeometry();

                if (link.getsNodePid() == nodePid) {
                    nodeGeoMap.put(nodePid, linkGeo.getStartPoint());
                } else if (link.geteNodePid() == nodePid) {
                    nodeGeoMap.put(nodePid, linkGeo.getEndPoint());
                }
            }
        }

        for (int nodePid : sameNodeMap.keySet()) {

            com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
                    null, this.conn);

            JSONObject json = new JSONObject();

            json.accumulate("objId", nodePid);

            json.accumulate("dbId", 0);

            json.accumulate("type", "RWNODE");

            JSONObject data = new JSONObject();

            Geometry nodeGeo=GeoTranslator.transform(nodeGeoMap.get(nodePid), GeoTranslator.dPrecisionMap, 5) ;

            data.accumulate("longitude", nodeGeo.getCoordinate().x);

            data.accumulate("latitude", nodeGeo.getCoordinate().y);

            json.accumulate("data", data);

            sameNodeOperation.moveMainNodeForTopo(json, ObjType.RWNODE, result);
        }

        return null;
    }


    /**
     * 立交
     * @param command
     * @param result
     * @param newLinks
     * @return
     * @throws Exception
     */
    public String handleRdGsc(Command command, Result result, List<RwLink> newLinks,RwLink updateLink)
            throws Exception {

        // 立交
        com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation gscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation();

        Map<Integer, Geometry> newLinkMap = new HashMap<>();

        for (RwLink link : newLinks) {
            newLinkMap.put(link.getPid(), link.getGeometry());
        }

        gscOperation.repairLink(command.getGscList(), newLinkMap, updateLink,
                result);

        return null;
    }

}
