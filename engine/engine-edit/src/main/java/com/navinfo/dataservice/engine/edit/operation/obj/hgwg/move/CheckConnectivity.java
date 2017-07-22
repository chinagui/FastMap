package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Title: Check1
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move
 * @Description: 该类用于移动坐标点位时判断是否满足条件
 *                  1. 原始LINK与目标LINK是否连通
 *                  2. 连通过程中是否有分叉路口
 *                  3. 移动距离是否小于指定距离(默认值100M)
 * @Author: Crayeres
 * @Date: 05/11/17
 * @Version: V1.0
 */
public class CheckConnectivity {

    private Connection conn;

    private String objName;

    private RdLink sourceLink;

    private Geometry sourceGeometry;

    private RdLink targetLink;

    private Geometry targetGeometry;

    private double validateLength;

    public CheckConnectivity(Connection conn, String objName, RdLink sourceLink, Geometry sourceGeometry, RdLink targetLink, Geometry
            targetGeometry) {
        this.conn = conn;
        this.objName = objName;
        this.sourceLink = sourceLink;
        this.sourceGeometry = sourceGeometry;
        this.targetLink = targetLink;
        this.targetGeometry = targetGeometry;

        this.validateLength = 100d;
    }

    /**
     *
     * Setter method for property <tt>validateLength</tt>.
     *
     * @param validateLength 移动校验长度
     */
    public void setValidateLength(double validateLength) {
        this.validateLength = validateLength;
    }

    public void check() throws Exception {
        if (1 == sourceLink.getDirect()) {
            String msg = this.handleUnidirectLink(sourceLink.getsNodePid());
            if (StringUtils.isNotEmpty(msg)) {
                msg = this.handleUnidirectLink(sourceLink.geteNodePid());
            }
            if (StringUtils.isNotEmpty(msg)) {
                throw new ServiceException(msg);
            }
        } else {
            String msg = "";
            if (2 == sourceLink.getDirect()) {
                msg = this.handleUnidirectLink(sourceLink.geteNodePid());

            } else if (3 == sourceLink.getDirect()) {
                msg = this.handleUnidirectLink(sourceLink.getsNodePid());
            }
            if (StringUtils.isNotEmpty(msg)) {
                throw new ServiceException(msg);
            }
        }
    }

    /**
     * 校验一个方向道路的移动操作
     * @param nodePid 起始点
     * @return
     * @throws Exception
     */
    private String handleUnidirectLink(int nodePid) throws Exception {
        List<RdLink> vialinks = this.listViaPath(sourceLink.pid(), nodePid, targetLink.pid());
        if (CollectionUtils.isEmpty(vialinks)) {
            return "原坐标与新坐标之间的link是非连通的link";
        }
        List<Integer> viaNodes = CalLinkOperateUtils.calNodePids(vialinks);
        if (this.hasForkLink(viaNodes)) {
            return String.format("通过改点位操作移动%s跨越分岔路口", objName);
        }
        double linkLength = this.calLinkLength(vialinks, viaNodes);
        if (validateLength < linkLength) {
            return String.format("通过改点位操作移动%s距离超过%.2fM", objName, validateLength);
        }
        return "";
    }

    /**
     * 计算移动长度
     * @param viaLinks 经过线
     * @param viaNodes 经过点
     * @return
     * @throws Exception
     */
    private double calLinkLength(List<RdLink> viaLinks, List<Integer> viaNodes) throws Exception {
        double sourceLength;
        double targetLength;
        double viaLength = 0d;

        int sourceNodePid;
        int targetNodePid;
        if (CollectionUtils.isEmpty(viaNodes)) {
            return getInnerLength();
        } else if (1 == viaNodes.size()) {
            sourceNodePid = targetNodePid = viaNodes.get(0);
        } else {
            sourceNodePid = viaNodes.get(0);
            targetNodePid = viaNodes.get(viaNodes.size() - 1);
        }

        sourceLength = getBlindLength(sourceLink, sourceGeometry, sourceNodePid);
        targetLength = getBlindLength(targetLink, targetGeometry, targetNodePid);
        if (2 < viaLinks.size()) {
            for (int i = 1; i < viaLinks.size() - 1; i++) {
                viaLength += GeometryUtils.getLinkLength(
                        GeoTranslator.transform(viaLinks.get(i).getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION));
            }
        }

        return sourceLength + targetLength + viaLength;
    }

    /**
     * 获取起点/终点重新组合后的线长度
     * @param link
     * @param geometry
     * @param nodePid
     * @return
     * @throws Exception
     */
    private double getBlindLength(RdLink link, Geometry geometry, int nodePid) throws Exception {
        Geometry linkGeometry = GeoTranslator.transform(link.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        List<Coordinate> coordinates = Arrays.asList(linkGeometry.getCoordinates());
        List<Coordinate> subCoordinates = new ArrayList<>();

        if (nodePid == link.getsNodePid()) {
            for (int i = coordinates.size() - 1; i >= 0; i--) {
                if (GeoTranslator.isIntersection(coordinates.get(i), coordinates.get(i - 1), geometry.getCoordinate())) {
                    subCoordinates = new ArrayList<>(coordinates.subList(0, i));
                    subCoordinates.add(geometry.getCoordinate());
                    break;
                }
            }
        } else if(nodePid == link.geteNodePid()) {
            for (int i = 0; i < coordinates.size() - 1; i++) {
                if (GeoTranslator.isIntersection(coordinates.get(i), coordinates.get(i + 1), geometry.getCoordinate())) {
                    subCoordinates = new ArrayList<>(coordinates.subList(i + 1, coordinates.size()));
                    subCoordinates.add(0, geometry.getCoordinate());
                    break;
                }
            }
        }
        return GeometryUtils.getLinkLength(GeoTranslator.createLineString(subCoordinates));
    }

    private double getInnerLength() throws Exception {
        Geometry linkGeometry = GeoTranslator.transform(sourceLink.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        List<Coordinate> coordinates = Arrays.asList(linkGeometry.getCoordinates());

        List<Coordinate> subCoordinates = new ArrayList<>();
        int start = 0;
        int end = 0;

        for (int i = 0; i < coordinates.size(); i++) {
            subCoordinates.add(coordinates.get(i));
            if (i == coordinates.size() - 1) {
                break;
            }

            if (GeoTranslator.isIntersection(coordinates.get(i), coordinates.get(i + 1), sourceGeometry.getCoordinate())) {
                start = i;
                subCoordinates.add(sourceGeometry.getCoordinate());
            }
            if (GeoTranslator.isIntersection(coordinates.get(i), coordinates.get(i + 1), targetGeometry.getCoordinate())) {
                end = i;
                subCoordinates.add(targetGeometry.getCoordinate());
            }
        }

        if (start <= end) {
            subCoordinates = subCoordinates.subList(start + 1, end + 3);
        } else {
            subCoordinates = subCoordinates.subList(end + 1, start + 3);
        }

        return GeometryUtils.getLinkLength(GeoTranslator.createLineString(subCoordinates));
    }

    /**
     * 获取经过线
     * @param sourceLinkPid 初始LINK
     * @param nodePid 方向NODE
     * @param targetLinkPid 目标LINK
     * @return
     */
    private List<RdLink> listViaPath(int sourceLinkPid, int nodePid, int targetLinkPid) {
        List<RdLink> viaLinks = null;
        List<Integer> linkPids = null;

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            String sql = "select * from table(package_utils.get_restrict_points(:1, :2, :3))";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, sourceLinkPid);
            pstmt.setInt(2, nodePid);
            pstmt.setString(3, String.valueOf(targetLinkPid));
            resultSet = pstmt.executeQuery();
            while(resultSet.next()) {
                String viaPath = resultSet.getString("via_path");
                if (StringUtils.isNotEmpty(viaPath)) {
                    linkPids = new ArrayList<>();
                    for (String pid : StringUtils.split(viaPath, ",")) {
                        linkPids.add(Integer.parseInt(pid));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        if (CollectionUtils.isNotEmpty(linkPids)) {
            try {
                viaLinks = new RdLinkSelector(conn).loadByPids(linkPids, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return viaLinks;
    }

    /**
     * 判断是否有分叉点
     * @param viaNodes
     * @return
     */
    private boolean hasForkLink(List<Integer> viaNodes) {
        boolean flag = false;

        if (CollectionUtils.isNotEmpty(viaNodes)) {
            try {
                Map<Integer, Integer> map = new RdNodeSelector(conn).calRdLinkCountOnNodes(viaNodes, false);
                for (Integer count : map.values()) {
                    if (2 < count) {
                        flag = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return flag;
    }
}
