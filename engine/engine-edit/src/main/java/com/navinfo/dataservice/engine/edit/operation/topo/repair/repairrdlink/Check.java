package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Check {

    // 形状点和形状点不能重合
    public void checkPointCoincide(double[][] ps) throws Exception {

        Set<String> set = new HashSet<String>();

        for (double[] p : ps) {
            set.add(p[0] + "," + ps[1]);
        }

        if (ps.length != set.size()) {
            throwException("形状点和形状点不能重合");
        }
    }

    // 对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点
    public void checkIsCrossNode(Connection conn, int nodePid) throws Exception {

        String sql = "select node_pid from rd_cross_node where node_pid = :1 and rownum =1";

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, nodePid);

        ResultSet resultSet = pstmt.executeQuery();

        boolean flag = false;

        if (resultSet.next()) {
            flag = true;
        }

        resultSet.close();

        pstmt.close();

        if (flag) {

            throwException("对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点");
        }
    }

    // 该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续
    public void checkIsVia(Connection conn, int linkPid) throws Exception {
        String sql = "select link_pid from rd_lane_via where link_pid =:1 and rownum=1 union all select link_pid " +
                "from" + " rd_restriction_via where link_pid =:2 and rownum=1 union all select link_pid from " +
                "rd_branch_via " + "where link_pid =:3 and rownum=1 ";

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, linkPid);

        pstmt.setInt(2, linkPid);

        pstmt.setInt(3, linkPid);

        ResultSet resultSet = pstmt.executeQuery();

        boolean flag = false;

        if (resultSet.next()) {
            flag = true;
        }

        resultSet.close();

        pstmt.close();

        if (flag) {

            throwException("该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续");
        }
    }

    // 相邻形状点不可过近，不能小于2m
    public void checkShapePointDistance(JSONObject geom) throws Exception {

        Geometry g = GeoTranslator.geojson2Jts(geom);

        Coordinate[] coords = g.getCoordinates();

        for (int i = 0; i < coords.length - 1; i++) {

            double distance = GeometryUtils.getDistance(coords[i].y, coords[i].x, coords[i + 1].y, coords[i + 1].x);

            if (distance <= 2) {
                throwException("相邻形状点不可过近，不能小于2m");
            }
        }
    }

    private void throwException(String msg) throws Exception {
        throw new Exception(msg);
    }

    public void checkCRFI(Connection conn, Command command) throws Exception {
        RdInterSelector selector = new RdInterSelector(conn);
        for (int i = 0; i < command.getCatchInfos().size(); i++) {
            JSONObject obj = command.getCatchInfos().getJSONObject(i);
            // 分离移动的node
            int nodePid = obj.getInt("nodePid");
            List<RdInter> inters = selector.loadInterByNodePid(String.valueOf(nodePid), false);

            if (!inters.isEmpty())
                throwException("此点做了CRFI信息，不允许移动");
        }
    }

    public void checkRdDirectRAndLaneC(Connection conn, Command command) throws Exception {
        RdCrossSelector selector = new RdCrossSelector(conn);
        RdDirectrouteSelector directrouteSelector = new RdDirectrouteSelector(conn);
        for (int i = 0; i < command.getCatchInfos().size(); i++) {
            JSONObject obj = command.getCatchInfos().getJSONObject(i);
            // 分离移动的node
            int nodePid = obj.getInt("nodePid");
            RdCross cross = null;
            try {
                cross = selector.loadCrossByNodePid(nodePid, false);
            } catch (Exception e) {
            }
            if (null != cross) {
                List<RdDirectroute> directroutes = directrouteSelector.getRestrictionByCrossPid(cross.pid(), false);
                if (!directroutes.isEmpty())
                    throwException("此点为路口点，不允许移动");

                RdLaneConnexitySelector laneConnexitySelector = new RdLaneConnexitySelector(conn);
                List<RdLaneConnexity> laneConnexities = laneConnexitySelector.getRdLaneConnexityByCrossPid(cross.pid
                        (), false);
                if (!laneConnexities.isEmpty())
                    throwException("此点为路口点，不允许移动");


                laneConnexities = laneConnexitySelector.loadByLink(command.getLinkPid(), 3, false);
                if (!laneConnexities.isEmpty()) {
                    List<Integer> linkPids = new RdLinkSelector(conn).loadLinkPidByNodePid(nodePid, false);
                    for (RdLaneConnexity laneConnexity : laneConnexities) {
                        for (RdLaneVia via : laneConnexity.viaMap.values()) {
                            if (linkPids.contains(via.getLinkPid())) {
                                throwException("此点为车信退出线的进入点，不允许移动");
                            }
                        }
                    }
                }
            }
        }
    }
}
