package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Crayeres on 2017/1/11 0011.
 */
public class Check {

    private Command command;

    private Connection conn;

    private RdLink sourceLink;

    private RdLink targetLink;

    private Geometry sourceGeo;

    private Geometry targetGeo;

    public Check(Command command, Connection conn, RdLink sourceLink, RdLink targetLink) {
        this.command = command;
        this.conn = conn;
        this.sourceLink = sourceLink;
        this.targetLink = targetLink;
        this.sourceGeo = GeoTranslator.transform(sourceLink.getGeometry(), 0.00001, 5);
        this.targetGeo = GeoTranslator.transform(targetLink.getGeometry(), 0.00001, 5);
    }

    public void precheck() throws Exception {
        checkConnected();
    }

    private void checkConnected() throws Exception {
        List<Integer> viaPids = new ArrayList<>();

        int direct = sourceLink.getDirect();
        int nodePid = 0;
        if (direct == 1 || direct == 2) {
            nodePid = sourceLink.geteNodePid();
        } else if (direct == 3) {
            nodePid = sourceLink.getsNodePid();
        }

        if (sourceLink.pid() == targetLink.pid()) {
            checkDistance(viaPids, 0);
            return;
        } else {
            if (direct == 1) {
                if (sourceLink.getsNodePid() == targetLink.getsNodePid() || sourceLink.getsNodePid() == targetLink
                        .geteNodePid()) {
                    final int pid = sourceLink.getsNodePid();
                    checkDistance(viaPids, pid);
                    checkNodeBifurcation(new ArrayList<Integer>() {{
                        add(pid);
                    }});
                    return;
                } else if (sourceLink.geteNodePid() == targetLink.getsNodePid() || sourceLink.geteNodePid() ==
                        targetLink.geteNodePid()) {
                    final int pid = sourceLink.geteNodePid();
                    checkDistance(viaPids, pid);
                    checkNodeBifurcation(new ArrayList<Integer>() {{
                        add(pid);
                    }});
                    return;
                }

            }
            if (direct == 2 && sourceLink.geteNodePid() == targetLink.getsNodePid()) {
                checkDistance(viaPids, sourceLink.geteNodePid());
                final int node = sourceLink.geteNodePid();
                checkNodeBifurcation(new ArrayList<Integer>() {{
                    add(node);
                }});
                return;
            }
            if (direct == 3 && sourceLink.getsNodePid() == targetLink.geteNodePid()) {
                checkDistance(viaPids, sourceLink.getsNodePid());
                final int node = sourceLink.getsNodePid();
                checkNodeBifurcation(new ArrayList<Integer>() {{
                    add(node);
                }});
                return;
            }
        }

        String sql = "select * from table(package_utils.get_restrict_points(:1,:2,:3))";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, sourceLink.pid());
        pstmt.setInt(2, nodePid);
        pstmt.setString(3, String.valueOf(targetLink.pid()));
        ResultSet resultSet = pstmt.executeQuery();
        while (resultSet.next()) {
            String viaPath = resultSet.getString("via_path");
            if (viaPath != null) {
                String[] splits = viaPath.split(",");
                for (String s : splits) {
                    if (!s.equals("")) {
                        int viaPid = Integer.valueOf(s);
                        if (viaPid == sourceLink.pid() || viaPid == targetLink.pid()) {
                            continue;
                        }
                        viaPids.add(viaPid);
                    }
                }
            }
        }
        if (viaPids.isEmpty()) {
            throw new Exception("原坐标与新坐标之间的link是非连通的link");
        }
        checkDistance(viaPids, nodePid);
    }


    private void checkDistance(List<Integer> viaPids, Integer nodePid) throws Exception {
        double length = 0;
        GeometryFactory factory = new GeometryFactory();
        List<Coordinate> sSubCoors = new ArrayList<>();
        List<Coordinate> eSubCoors = new ArrayList<>();
        Coordinate sourceCoor = GeoTranslator.transform(command.getHgwgLimit().getGeometry(), 0.00001, 5)
                .getCoordinate();
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{command.getContent().getDouble("longitude"), command.getContent()
                .getDouble("latitude")});
        Coordinate targetCoor = GeoTranslator.geojson2Jts(geoPoint).getCoordinate();

        Coordinate[] sCoors = sourceGeo.getCoordinates();
        Coordinate[] eCoors = targetGeo.getCoordinates();

        int direct = sourceLink.getDirect();
        if (nodePid != 0) {
            if (direct == 1) {
                if (sourceLink.geteNodePid() == nodePid) {
                    length += checkForwardDistance(factory, sSubCoors, eSubCoors, sourceCoor, targetCoor, sCoors,
                            eCoors);
                } else if (sourceLink.getsNodePid() == nodePid) {
                    length += checkReverseDistance(factory, sSubCoors, eSubCoors, sourceCoor, targetCoor, sCoors,
                            eCoors);
                }
            } else if (direct == 2) {
                length += checkForwardDistance(factory, sSubCoors, eSubCoors, sourceCoor, targetCoor, sCoors, eCoors);
            } else if (direct == 3) {
                length += checkReverseDistance(factory, sSubCoors, eSubCoors, sourceCoor, targetCoor, sCoors, eCoors);
            }
        }

        if (viaPids.isEmpty()) {
            if (nodePid == 0) {
                boolean findS = false;
                for (int i = 0; i < sCoors.length - 1; i++) {
                    if (sCoors.length == 2) {
                        sSubCoors.add(sourceCoor);
                        sSubCoors.add(targetCoor);
                        break;
                    }
                    if (GeoTranslator.isIntersection(sCoors[i], sCoors[i + 1], sourceCoor)) {
                        if (findS) {
                            sSubCoors.add(sourceCoor);
                        } else {
                            sSubCoors.add(sourceCoor);
                            sSubCoors.add(sCoors[i + 1]);
                            findS = true;
                        }
                    } else if (GeoTranslator.isIntersection(sCoors[i], sCoors[i + 1], targetCoor)) {
                        if (findS) {
                            sSubCoors.add(targetCoor);
                        } else {
                            sSubCoors.add(targetCoor);
                            sSubCoors.add(sCoors[i + 1]);
                            findS = true;
                        }
                    }
                }
                length += factory.createLineString(sSubCoors.toArray(new Coordinate[]{})).getLength();
                if (length > 100) {
                    throw new Exception("通过改点位操作移动限高限重距离超过100m");
                }
            } else {
                if (length > 100) {
                    throw new Exception("通过改点位操作移动限高限重距离超过100m");
                }
            }
        } else {
            List<RdLink> viaLinks = new RdLinkSelector(conn).loadByPids(viaPids, false);
            for (RdLink link : viaLinks) {
                length += link.getLength();
                if (length > 100) {
                    throw new Exception("通过改点位操作移动限高限重距离超过100m");
                }
            }
            checkLinkBifurcation(viaLinks);
        }
    }

    private double checkForwardDistance(GeometryFactory factory, List<Coordinate> sSubCoors, List<Coordinate>
            eSubCoors, Coordinate sourceCoor, Coordinate targetCoor, Coordinate[] sCoors, Coordinate[] eCoors) throws
            Exception {
        double length = 0;
        boolean flag = false;
        for (int i = 0; i < sCoors.length - 1; i++) {
            if (!flag && GeoTranslator.isIntersection(sCoors[i], sCoors[i + 1], sourceCoor)) {
                sSubCoors.add(sourceCoor);
                flag = true;
            }
            if (flag) {
                sSubCoors.add(sCoors[i + 1]);
            }
        }
        Geometry sSubGeo = factory.createLineString(sSubCoors.toArray(new Coordinate[]{}));
        length += GeometryUtils.getLinkLength(sSubGeo);

        flag = false;
        for (int i = eCoors.length - 1; i > 0; i--) {
            if (!flag && GeoTranslator.isIntersection(eCoors[i], eCoors[i - 1], targetCoor)) {
                eSubCoors.add(targetCoor);
                flag = true;
            }
            if (flag) {
                eSubCoors.add(eCoors[i - 1]);
            }
        }
        Geometry eSubGeo = factory.createLineString(eSubCoors.toArray(new Coordinate[]{}));
        length += GeometryUtils.getLinkLength(eSubGeo);

        return length;
    }

    private double checkReverseDistance(GeometryFactory factory, List<Coordinate> sSubCoors, List<Coordinate>
            eSubCoors, Coordinate sourceCoor, Coordinate targetCoor, Coordinate[] sCoors, Coordinate[] eCoors) throws
            Exception {
        double length = 0;
        boolean flag = false;
        for (int i = 0; i < eCoors.length - 1; i++) {
            if (!flag && GeoTranslator.isIntersection(eCoors[i], eCoors[i + 1], targetCoor)) {
                eSubCoors.add(targetCoor);
                flag = true;
            }
            if (flag) {
                eSubCoors.add(eCoors[i + 1]);
            }
        }
        Geometry eSubGeo = factory.createLineString(eSubCoors.toArray(new Coordinate[]{}));
        length += GeometryUtils.getLinkLength(eSubGeo);

        flag = false;
        for (int i = sCoors.length - 1; i > 0; i--) {
            if (!flag && GeoTranslator.isIntersection(sCoors[i], sCoors[i - 1], sourceCoor)) {
                sSubCoors.add(sourceCoor);
                flag = true;
            }
            if (flag) {
                sSubCoors.add(sCoors[i - 1]);
            }
        }
        Geometry sSubGeo = factory.createLineString(sSubCoors.toArray(new Coordinate[]{}));
        length += GeometryUtils.getLinkLength(sSubGeo);

        return length;
    }

    private void checkLinkBifurcation(List<RdLink> viaLinks) throws Exception {
        Set<Integer> viaNodePids = new HashSet<>();
        for (RdLink link : viaLinks) {
            viaNodePids.add(link.getsNodePid());
            viaNodePids.add(link.geteNodePid());
        }
        ArrayList<Integer> tmpList = new ArrayList<Integer>();
        tmpList.addAll(viaNodePids);
        checkNodeBifurcation(tmpList);
    }

    private void checkNodeBifurcation(List<Integer> viaNodes) throws Exception {
        RdNodeSelector selector = new RdNodeSelector(conn);
        Map<Integer, Integer> map = selector.calRdLinkCountOnNodes(viaNodes, false);
        for (Integer count : map.values()) {
            if (count > 2) {
                throw new Exception("通过改点位操作移动限高限重跨越分岔路口");
            }
        }
    }
}
