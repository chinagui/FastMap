package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

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

        if (nodePid <= 0) {
            return;
        }

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

    //该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续
    public void checkIsVia(Connection conn, int linkPid) throws Exception {
        String sql = "SELECT LINK_PID FROM RD_LANE_VIA WHERE LINK_PID = :1 AND ROWNUM = 1 AND U_RECORD != 2 UNION " +
                "ALL" + " SELECT LINK_PID FROM RD_RESTRICTION_VIA WHERE LINK_PID = :2 AND ROWNUM = 1 AND U_RECORD != " +
                "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "2 UNION " + "ALL SELECT LINK_PID FROM " +
                "RD_VOICEGUIDE_VIA" + " " + "WHERE " + "LINK_PID" + " = :3" + " AND " + "" + "ROWNUM = 1" + " AND " +
                "" + "U_RECORD != 2 " + "UNION ALL " + "SELECT LINK_PID " + "FROM " + "RD_BRANCH_VIA " + "WHERE " +
                "LINK_PID = :4 " + "AND" + " " + "ROWNUM " + "= 1 " + "" + "AND U_RECORD != " + "2 " + "UNION ALL " +
                "SELECT " + "LINK_PID FROM " + "RD_DIRECTROUTE_VIA " + "WHERE LINK_PID = " + ":5 AND" + " " +
                "ROWNUM" + " = 1 AND U_RECORD" + " !=" + " 2";

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, linkPid);

        pstmt.setInt(2, linkPid);

        pstmt.setInt(3, linkPid);

        pstmt.setInt(4, linkPid);

        pstmt.setInt(5, linkPid);

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

    //相邻形状点不可过近，不能小于2m
    public void checkShapePointDistance(double[][] ps) throws Exception {

        for (int i = 0; i < ps.length - 1; i++) {

            double smx = MercatorProjection.longitudeToMetersX(ps[i][0]);

            double smy = MercatorProjection.latitudeToMetersY(ps[i][0]);

            double emx = MercatorProjection.longitudeToMetersX(ps[i + 1][0]);

            double emy = MercatorProjection.latitudeToMetersY(ps[i + 1][0]);

            double distance = Math.pow(smx - emx, 2) + Math.pow(smy - emy, 2);

            if (distance < 4) {
                throwException("相邻形状点不可过近，不能小于2m");
            }

        }

    }

    private void throwException(String msg) throws Exception {
        throw new Exception(msg);
    }

    public void checkCRFI(Connection conn, Integer nodePid) throws Exception {
        RdInterSelector selector = new RdInterSelector(conn);
        List<RdInter> inters = selector.loadInterByNodePid(String.valueOf(nodePid), false);
        if (!inters.isEmpty())
            throwException("此点做了CRFI信息，不允许移动");
    }

    public void checkRdDirectRAndLaneC(Connection conn, Integer nodePid) throws Exception {
        RdCrossSelector selector = new RdCrossSelector(conn);
        RdCross cross = null;
        try {
            cross = selector.loadCrossByNodePid(nodePid, false);
        } catch (Exception e) {
        }
        if (null != cross) {
            RdDirectrouteSelector directrouteSelector = new RdDirectrouteSelector(conn);
            List<RdDirectroute> directroutes = directrouteSelector.getRestrictionByCrossPid(cross.pid(), false);
            if (!directroutes.isEmpty())
                throwException("此点为路口点，不允许移动");
            RdLaneConnexitySelector laneConnexitySelector = new RdLaneConnexitySelector(conn);
            List<RdLaneConnexity> laneConnexities = laneConnexitySelector.getRdLaneConnexityByCrossPid(cross.pid(),
                    false);
            if (!laneConnexities.isEmpty())
                throwException("此点为路口点，不允许移动");
        }
    }

}
