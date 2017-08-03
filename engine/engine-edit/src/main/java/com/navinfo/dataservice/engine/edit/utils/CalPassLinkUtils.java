package com.navinfo.dataservice.engine.edit.utils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by ly on 2017/6/13.
 */
public class CalPassLinkUtils {

    //外扩距离
    private static final double bufferLength = 200;

    private Connection conn;

    //需要计算的节点
    List<NodePoint> activityNodePoint = new ArrayList<>();

    //端点通达信息。Integer当前节点pid， List<Integer>：当前节点可通行节点的pid列表
    Map<Integer, List<Integer>> p2p = new HashMap<>();

    // String:起始节点PID_终止节点PID，linkinfo：起终节点组成link的信息
    Map<String, LinkInfo> linkInfos = new HashMap<>();

    //Integer节点PID，NodePoint：节点信息
    Map<Integer, NodePoint> nodePointMap = new HashMap<>();

    public CalPassLinkUtils(Connection conn) {

        this.conn = conn;
    }

    private void init() {
        activityNodePoint.clear();

        p2p.clear();

        linkInfos.clear();

        nodePointMap.clear();
    }

    /**
     * 计算经过线
     * @param inLink
     * @param outLinkPid
     * @return
     * @throws Exception
     */
    public List<Integer> calcPassLinks(RdLink inLink, int outLinkPid)
            throws Exception {

        if (inLink.getDirect() == 2) {

            return calcPassLinks(inLink.getPid(), inLink.geteNodePid(), outLinkPid);

        } else if (inLink.getDirect() == 3) {

            return calcPassLinks(inLink.getPid(), inLink.getsNodePid(), outLinkPid);
        }

        RdLinkSelector selector = new RdLinkSelector(this.conn);

        List<Integer> pids = Arrays.asList(outLinkPid);

        List<RdLink> sourceLinks = selector.loadByPids(pids, true);

        RdLink endLink = sourceLinks.get(0);

        calcNodePointInfo(inLink, inLink.getsNodePid(), endLink);

        NodePoint sNodePoint = getMinNodePoint(endLink);

        double sLength = sNodePoint == null ? Double.MAX_VALUE : sNodePoint.total;

        calcNodePointInfo(inLink, inLink.geteNodePid(), endLink);

        NodePoint eNodePoint = getMinNodePoint(endLink);

        double eLength = eNodePoint == null ? Double.MAX_VALUE : eNodePoint.total;

        if (eLength == Double.MAX_VALUE && sLength == Double.MAX_VALUE) {

            throw new Exception("未计算出经过线");
        }

        if (sLength < eLength) {

            return getPassLink(sNodePoint);

        } else {

            return getPassLink(eNodePoint);
        }
    }

    public List<Integer> calcPassLinks(int inLinkPid, int nodePid, int outLinkPid) throws Exception {

        RdLinkSelector selector = new RdLinkSelector(this.conn);

        List<Integer> pids = Arrays.asList(inLinkPid, outLinkPid);

        List<RdLink> sourceLinks = selector.loadByPids(pids, true);

        RdLink inLink = sourceLinks.get(0).getPid() == inLinkPid ? sourceLinks.get(0) : sourceLinks.get(1);

        RdLink endLink = sourceLinks.get(0).getPid() == outLinkPid ? sourceLinks.get(0) : sourceLinks.get(1);

        calcNodePointInfo(inLink, nodePid, endLink);

        NodePoint nodePoint = getMinNodePoint(endLink);

        if (nodePoint == null) {

            throw new Exception("未计算出经过线，请手动选择经过线");
        }

        List<Integer> passLinkPids = getPassLink(nodePoint);

        init();

        return passLinkPids;
    }

    /**
     * 计算节点信息
     * @param inLink
     * @param nodePid
     * @param endLink
     * @return
     * @throws Exception
     */
    private void calcNodePointInfo(RdLink inLink, int nodePid, RdLink endLink) throws Exception {

        init();

        if ((endLink.getDirect() == 2 && endLink.getsNodePid() == nodePid)
                || (endLink.getDirect() == 3 && endLink.geteNodePid() == nodePid)
                || (endLink.getDirect() == 1 && (endLink.getsNodePid() == nodePid || endLink.geteNodePid() == nodePid))) {

            return ;
        }

        String wktBuffer = getWktBuffer(Arrays.asList(inLink, endLink));

        searchLinkBySpatial(wktBuffer, inLink.getPid(), endLink.getPid());

        NodePoint firstNodePoint = new NodePoint(nodePid, inLink.getPid());

        activityNodePoint.add(firstNodePoint);

        nodePointMap.put(firstNodePoint.getNodePid(), firstNodePoint);

        calNodePoint();
    }

    /**
     * 获取Link组的最小外包矩形wkt
     *
     * @param sourceLinks 源link
     * @return 坐标数组
     * @throws ParseException
     */
    private String getWktBuffer(List<RdLink> sourceLinks) throws Exception {

        List<Coordinate> coordinates = new ArrayList<>();

        for (RdLink link : sourceLinks) {

            Geometry geo = GeoTranslator.transform(link.getGeometry(), GeoTranslator.dPrecisionMap, 5);

            for (Coordinate coordinate : geo.getCoordinates()) {

                coordinates.add(coordinate);
            }
        }

        double minLon = 180;

        double minLat = 90;

        double maxLon = 0;

        double maxLat = 0;

        for (Coordinate c : coordinates) {

            if (minLon > c.x) {
                minLon = c.x;
            }

            if (maxLon < c.x) {
                maxLon = c.x;
            }

            if (minLat > c.y) {
                minLat = c.y;
            }

            if (maxLat < c.y) {
                maxLat = c.y;
            }
        }

        GeometryFactory factory = new GeometryFactory();

        Coordinate[] coordinatesBuffer = new Coordinate[5];

        coordinatesBuffer[0] = new Coordinate(minLon, minLat);

        coordinatesBuffer[1] = new Coordinate(minLon, maxLat);

        coordinatesBuffer[2] = new Coordinate(maxLon, maxLat);

        coordinatesBuffer[3] = new Coordinate(maxLon, minLat);

        coordinatesBuffer[4] = new Coordinate(minLon, minLat);

        Geometry polygon = factory.createPolygon(coordinatesBuffer);

        Geometry buffer = polygon.buffer(GeometryUtils.convert2Degree(bufferLength));

        return GeoTranslator.jts2Wkt(buffer);
    }

    /**
     * 根据多边形获取link信息
     * @param wkt 查询几何范围
     * @param inLinkPid 退出线
     * @param outLinkPid 退出线
     * @throws Exception
     */
    private void searchLinkBySpatial(String wkt, int inLinkPid, int outLinkPid) throws Exception {

        String sql = "SELECT A.LINK_PID, A.LENGTH, A.DIRECT, A.S_NODE_PID, A.E_NODE_PID FROM RD_LINK A WHERE SDO_WITHIN_DISTANCE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND A.U_RECORD != 2";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, wkt);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                int direct = resultSet.getInt("DIRECT");

                int sNodePid = resultSet.getInt("S_NODE_PID");

                int eNodePid = resultSet.getInt("E_NODE_PID");

                int linkPid = resultSet.getInt("LINK_PID");

                double length = resultSet.getDouble("LENGTH");

                if (linkPid == outLinkPid||linkPid == inLinkPid) {

                    continue;
                }

                LinkInfo info = new LinkInfo(length, linkPid);

                if (direct == 2 || direct == 1) {

                    if (!p2p.containsKey(sNodePid)) {

                        p2p.put(sNodePid, new ArrayList<Integer>());
                    }

                    p2p.get(sNodePid).add(eNodePid);

                    linkInfos.put(sNodePid + "_" + eNodePid, info);
                }
                if (direct == 3 || direct == 1) {

                    if (!p2p.containsKey(eNodePid)) {

                        p2p.put(eNodePid, new ArrayList<Integer>());
                    }

                    p2p.get(eNodePid).add(sNodePid);

                    linkInfos.put(eNodePid + "_" + sNodePid, info);
                }
            }
        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DBUtils.closeStatement(pstmt);
            DBUtils.closeResultSet(resultSet);
        }
    }

    /**
     * 生成节点信息
     */
    private void calNodePoint() {

        //防止死循环，int值域较小，采用long
        long count = 0;

        while (activityNodePoint.size() > 0 && count < Long.MAX_VALUE) {

            count++;

            //下一轮被激活的节点
            List<NodePoint> activityNext = new ArrayList<>();

            for (NodePoint nodePoint : activityNodePoint) {

                //无后续节点continue
                if (!p2p.containsKey(nodePoint.getNodePid())) {

                    continue;
                }

                List<Integer> nextNodePids = p2p.get(nodePoint.getNodePid());

                for (int nextNodePid : nextNodePids) {

                    //对首次出现的node创建节点
                    if (!nodePointMap.containsKey(nextNodePid)) {

                        NodePoint nodePointAdd = new NodePoint(nextNodePid);

                        nodePointMap.put(nodePointAdd.getNodePid(), nodePointAdd);
                    }

                    NodePoint nextNodePoint = nodePointMap.get(nextNodePid);

                    boolean isActivity = nextNodePoint.setPreNodePoint(nodePoint);

                    if (isActivity) {

                        activityNext.add(nextNodePoint);
                    }
                }
            }

            activityNodePoint = activityNext;
        }
    }

    private NodePoint getMinNodePoint(RdLink endLink) throws Exception
    {
        NodePoint nodePoint = null;

        if ((endLink.getDirect() == 1 || endLink.getDirect() == 2)
                && nodePointMap.containsKey(endLink.getsNodePid())) {

            nodePoint = nodePointMap.get(endLink.getsNodePid());
        }

        if ((endLink.getDirect() == 1 || endLink.getDirect() == 3)
                && nodePointMap.containsKey(endLink.geteNodePid())) {

            NodePoint tmp = nodePointMap.get(endLink.geteNodePid());

            if (nodePoint == null || nodePoint.total > tmp.total) {

                nodePoint = tmp;
            }
        }

        return nodePoint;
    }

    /**
     * 获取经过线
     * @param nodePoint 最优节点
     * @return
     * @throws Exception
     */
    private List<Integer> getPassLink( NodePoint nodePoint) throws Exception {

        List<Integer> passLinkPids = new ArrayList<>();

        while (nodePoint.getPreNodePoint() != null) {

            passLinkPids.add(nodePoint.linkPid);

            nodePoint = nodePoint.getPreNodePoint();
        }

        Collections.reverse(passLinkPids);

        return passLinkPids;
    }


    /*********************************************辅助类******************************************/

    /**
     * link信息
     */
    public class LinkInfo {

        public double length;

        public int linkPid;

        public LinkInfo(double length, int linkPid) {

            this.length = length;

            this.linkPid = linkPid;
        }
    }

    /**
     * node节点
     */
    public class NodePoint {

        //当前节点nodePid
        private int nodePid;

        //上一节点与当前nodePid的组成link的Pid
        private int linkPid;

        private double total = Double.MAX_VALUE;

        //上一节点
        NodePoint preNodePoint;

        /**
         * 创建非进入点NodePoint
         *
         * @param nodePid 当前节点nodePid
         */
        public NodePoint(int nodePid) {

            this.nodePid = nodePid;
        }

        /**
         * 创建进入点NodePoint
         *
         * @param nodePid 进入点Pid
         * @param linkPid 进入线Pid
         */
        public NodePoint(int nodePid, int linkPid) {

            this.nodePid = nodePid;

            this.linkPid = linkPid;

            this.total = 0;

            preNodePoint = null;
        }

        public int getNodePid() {

            return nodePid;
        }

        public NodePoint getPreNodePoint() {

            return preNodePoint;
        }

        /**
         * 设置前节点
         * @param preNodePoint
         * @return 前节点被替换返回true，否则返回false
         */
        public boolean setPreNodePoint(NodePoint preNodePoint) {

            LinkInfo linkInfo = linkInfos.get(preNodePoint.nodePid + "_" + nodePid);

            double currTotal = preNodePoint.total + linkInfo.length;

            if (currTotal < this.total) {

                this.preNodePoint = preNodePoint;

                total = preNodePoint.total + linkInfo.length;

                linkPid = linkInfo.linkPid;

                return true;
            }

            return false;
        }

    }


}
