package com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.druid.util.StringUtils;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.ZoneLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONObject;

/**
 * @author zhaokk 行政区划面有关操作
 */
public class Operation implements IOperation {

    private Command command;
    private Result result;
    private ZoneFace face;
    private boolean updateFlag = true;
    private Connection conn;

    public Operation(Connection conn, Result result) {
        this.conn = conn;
        this.result = result;
    }

    public Operation(Connection conn, Result result, ZoneFace face) {
        this.conn = conn;
        this.face = face;
        this.result = result;
    }

    public Operation(Command command, Result result) {
        this.command = command;
        this.result = result;
        this.updateFlag = false;
    }

    @Override
    public String run(Result result) throws Exception {

        // 既有线构成面
        if (command.getLinkPids() != null) {
            // ZONELINK
            if (command.getLinkType().equals(ObjType.ZONELINK.toString())) {
                this.createFaceByZoneLink(command.getLinks());
            }
            // RDLINK
            if (command.getLinkType().equals(ObjType.RDLINK.toString())) {
                // 根据RDLINK生成ZONELINK
                Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
                List<IObj> zoneLinks = new ArrayList<IObj>();
                for (IObj obj : command.getLinks()) {
                    RdLink link = (RdLink) obj;
                    zoneLinks.add(this.createLinkOfFace(GeoTranslator.transform(link.getGeometry(), 0.00001, 5), maps));
                }
                this.createFaceByZoneLink(zoneLinks);
            }
        }
        // 创建
        if (command.getGeometry() != null) {
            this.createFaceByGeometry(result);
        }

        return null;
    }

    /**
     * 根据既有线创建面
     *
     * @param objList 传入要创建面的几何
     * @throws Exception
     */
    public void createFaceByZoneLink(List<IObj> objList) throws Exception {
        Set<String> meshes = new HashSet<String>();
        List<ZoneLink> zoneLinks = new ArrayList<ZoneLink>();
        for (IObj obj : objList) {
            ZoneLink link = (ZoneLink) obj;
            zoneLinks.add(link);
            if (link.getMeshes().size() == 1) {
                for (IRow iRow : link.getMeshes()) {
                    ZoneLinkMesh adlinkmesh = (ZoneLinkMesh) iRow;
                    if (adlinkmesh.getMeshId() != 0) {
                        meshes.add(String.valueOf(adlinkmesh.getMeshId()));
                    }
                }
            }
        }
        if (meshes.size() == 1) {
            int meshId = Integer.parseInt(meshes.iterator().next());
            this.createFace();
            this.face.setMeshId(meshId);
            this.face.setMesh(meshId);
            this.reCaleFaceGeometry(zoneLinks);
        } else {
            this.updateFlag = false;
            Geometry geom = GeoTranslator.transform(this.getPolygonGeometry(zoneLinks), 0.00001, 5);
            this.createFaceWithMesh(meshes, geom, objList, 1);
        }
    }

    /**
     * @param meshes 跨域图幅
     * @param geom   初始画面几何
     * @param flag   创建面表示 0 根据几何，1 根据既有线
     * @throws Exception
     */
    private void createFaceWithMesh(Set<String> meshes, Geometry geom, List<IObj> objList, int flag) throws Exception {
        Iterator<String> it = meshes.iterator();
        Map<Coordinate, Integer> mapNode = new HashMap<Coordinate, Integer>();
        Map<Geometry, ZoneLink> mapLink = new HashMap<Geometry, ZoneLink>();
        if (flag == 1) {
            for (IObj obj : objList) {
                ZoneLink zoneLink = (ZoneLink) obj;
                Geometry geometry = GeoTranslator.transform(zoneLink.getGeometry(), 0.00001, 5);
                mapLink.put(geometry, zoneLink);

                if (!mapNode.containsKey(geometry.getCoordinates()[0])) {
                    mapNode.put(geometry.getCoordinates()[0], zoneLink.getsNodePid());
                }
                if (!mapNode.containsKey(geometry.getCoordinates()[geometry.getCoordinates().length - 1])) {
                    mapNode.put(geometry.getCoordinates()[geometry.getCoordinates().length - 1], zoneLink.geteNodePid());
                }
            }
        }
        while (it.hasNext()) {
            String meshIdStr = it.next();
            // 获取每个图幅中闭合线的数组
            Set<LineString[]> set = CompGeometryUtil.cut(JtsGeometryFactory.createPolygon(geom.getCoordinates()), meshIdStr);
            Iterator<LineString[]> itLine = set.iterator();
            while (itLine.hasNext()) {
                LineString[] lineStrings = itLine.next();
                List<ZoneLink> links = new ArrayList<ZoneLink>();
                for (LineString lineString : lineStrings) {
                    ZoneLink zoneLink = null;
                    if (MeshUtils.isMeshLine(lineString)) {
                        if (mapLink.containsKey(lineString.reverse())) {
                            zoneLink = mapLink.get(lineString.reverse());
                        } else if (mapLink.containsKey(lineString)) {
                            zoneLink = mapLink.get(lineString);
                        } else {
                            zoneLink = this.createLinkOfFace(lineString, mapNode);
                            mapLink.put(lineString, zoneLink);
                        }
                        links.add(zoneLink);
                    } else {
                        if (flag == 0) {
                            if (mapLink.containsKey(lineString)) {
                                zoneLink = mapLink.get(lineString);
                            } else {
                                zoneLink = this.createLinkOfFace(lineString, mapNode);
                                mapLink.put(lineString, zoneLink);
                            }
                            links.add(zoneLink);
                        } else {

                            Iterator<Geometry> itLinks = mapLink.keySet().iterator();
                            while (itLinks.hasNext()) {
                                Geometry g = itLinks.next();
                                if (lineString.contains(g)) {
                                    links.add(mapLink.get(g));
                                }

                            }

                        }

                    }

                }
                // 创建线
                this.createFace();
                this.reCaleFaceGeometry(links);
            }

        }

    }

    /**
     * 按照几何形状生成面
     *
     * @param result
     * @throws Exception
     */
    public void createFaceByGeometry(Result result) throws Exception {
        Geometry geom = GeoTranslator.geojson2Jts(command.getGeometry(), 1, 5);
        Coordinate sPoint = geom.getCoordinates()[0];
        // 获取几何形状跨越图幅号
        Set<String> meshes = new HashSet<String>();
        meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geom);
        // 如果不跨图幅
        if (meshes.size() == 1) {
            // 生成起始node
            ZoneNode Node = (ZoneNode) NodeOperateUtils.createNode(sPoint.x, sPoint.y, ObjType.ZONENODE);
            result.insertObject(Node, ObjStatus.INSERT, Node.pid());
            this.createFace();
            List<ZoneLink> links = new ArrayList<ZoneLink>();
            links.add(ZoneLinkOperateUtils.getAddLink(geom, Node.getPid(), Node.getPid(), result, null));
            this.reCaleFaceGeometry(links);
        }// 如果跨图幅
        else {
            this.createFaceWithMesh(meshes, geom, null, 0);
        }

    }

    private ZoneLink createLinkOfFace(Geometry g, Map<Coordinate, Integer> maps) throws Exception {
        int sNodePid = 0;
        int eNodePid = 0;
        if (maps.containsKey(g.getCoordinates()[0])) {
            sNodePid = maps.get(g.getCoordinates()[0]);
        }
        if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
            eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
        }
        JSONObject node = ZoneLinkOperateUtils.createZoneNodeForLink(g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        return ZoneLinkOperateUtils.getAddLink(g, (int) node.get("s"), (int) node.get("e"), result, null);
    }

    /*
     * 添加Link和FaceTopo关系
     */
    public void addLink(Map<ZoneLink, Integer> map) {
        List<IRow> zoneFaceTopos = new ArrayList<IRow>();
        for (ZoneLink link : map.keySet()) {
            ZoneFaceTopo faceTopo = new ZoneFaceTopo();
            faceTopo.setLinkPid(link.getPid());
            faceTopo.setFacePid(face.getPid());
            faceTopo.setSeqNum(map.get(link));
            zoneFaceTopos.add(faceTopo);
        }
        this.face.setFaceTopos(zoneFaceTopos);
    }

    /*
     * 添加Link和FaceTopo关系
     */
    public void createFaceTop(Map<ZoneLink, Integer> map) {
        List<IRow> adFaceTopos = new ArrayList<IRow>();
        for (ZoneLink link : map.keySet()) {
            ZoneFaceTopo faceTopo = new ZoneFaceTopo();
            faceTopo.setLinkPid(link.getPid());
            faceTopo.setFacePid(face.getPid());
            faceTopo.setSeqNum(map.get(link));
            adFaceTopos.add(faceTopo);
            result.insertObject(faceTopo, ObjStatus.INSERT, face.getPid());
        }

    }

    /*
     * @param List 按照ADFACE的形状重新维护ADFACE
     */
    @SuppressWarnings("null")
    public void reCaleFaceGeometry(List<ZoneLink> links) throws Exception {
        if (links != null && links.size() < 1) {
            throw new Exception("重新维护面的形状:发现面没有组成link");
        }
        ZoneLink currLink = null;
        for (ZoneLink zoneLink : links) {
            currLink = zoneLink;
            break;
        }
        if (currLink == null) {
            return;
        }
        // 获取当前LINK和NODE
        int startNodePid = currLink.getsNodePid();
        int currNodePid = startNodePid;
        Map<ZoneLink, Integer> map = new HashMap<ZoneLink, Integer>();
        map.put(currLink, 1);
        int index = 1;
        List<Geometry> list = new ArrayList<Geometry>();
        list.add(currLink.getGeometry());
        Map<Integer, ZoneLink> currLinkAndPidMap = new HashMap<Integer, ZoneLink>();
        currLinkAndPidMap.put(currNodePid, currLink);
        // 获取下一条联通的LINK
        while (ZoneLinkOperateUtils.getNextLink(links, currLinkAndPidMap)) {
            if (currLinkAndPidMap.keySet().iterator().next() == startNodePid) {
                break;
            }
            index++;
            map.put(currLinkAndPidMap.get(currLinkAndPidMap.keySet().iterator().next()), index);
            list.add(currLinkAndPidMap.get(currLinkAndPidMap.keySet().iterator().next()).getGeometry());

        }
        // 线几何组成面的几何
        if (this.updateFlag) {
            this.createFaceTop(map);
        } else {
            this.addLink(map);
        }
        Geometry g = GeoTranslator.getCalLineToPython(list);
        Coordinate[] c1 = new Coordinate[g.getCoordinates().length];
        // 判断线组成面是否可逆
        if (!GeometryUtils.IsCCW(g.getCoordinates())) {
            for (int i = g.getCoordinates().length - 1; i >= 0; i--) {
                c1[c1.length - i - 1] = g.getCoordinates()[i];
            }
            this.reverseFaceTopo();

        } else {
            c1 = g.getCoordinates();
        }
        Geometry faceGeo = GeoTranslator.getPolygonToPoints(c1);
        if (null != face) {
            if (face.getRegionId() != 0) {
                AdAdminSelector selector = new AdAdminSelector(conn);
                AdAdmin admin = (AdAdmin) selector.loadById(face.getRegionId(), false);
                if (null != admin && (admin.getAdminType() == 8 || admin.getAdminType() == 9))
                    ZoneIDBatchUtils.updateZoneID(face, faceGeo, face.getMeshId(), conn, result);
            }
        }
        // 更新面的几何属性
        if (this.updateFlag) {
            this.updateGeometry(faceGeo, this.face);
        } else {
            this.createFaceGeometry(faceGeo, this.face);
        }
    }

    /**
     * 根据传入的link 重组PolygonGeometry
     */
    private Geometry getPolygonGeometry(List<ZoneLink> links) throws Exception {
        if (links != null && links.size() < 1) {
            throw new Exception("重新维护面的形状:发现面没有组成link");
        }
        ZoneLink currLink = null;
        for (ZoneLink zoneLink : links) {
            currLink = zoneLink;
            break;
        }
        if (currLink == null) {
            throw new Exception("重新维护面的形状:发现面没有组成link");
        }
        // 获取当前LINK和NODE
        int startNodePid = currLink.getsNodePid();
        int currNodePid = startNodePid;
        List<Geometry> list = new ArrayList<Geometry>();
        list.add(currLink.getGeometry());
        Map<Integer, ZoneLink> currLinkAndPidMap = new HashMap<Integer, ZoneLink>();
        currLinkAndPidMap.put(currNodePid, currLink);
        // 获取下一条联通的LINK
        while (ZoneLinkOperateUtils.getNextLink(links, currLinkAndPidMap)) {
            if (currLinkAndPidMap.keySet().iterator().next() == startNodePid) {
                break;
            }
            list.add(currLinkAndPidMap.get(currLinkAndPidMap.keySet().iterator().next()).getGeometry());

        }

        return GeoTranslator.getCalLineToPython(list);

    }

    /*
     * 更新面的几何属性
     */
    private void createFaceGeometry(Geometry g, ZoneFace face) throws Exception {
        face.setGeometry(g);
        // 缩放计算面积和周长
        g = GeoTranslator.transform(g, 0.00001, 5);
        String meshId = CompGeometryUtil.geoToMeshesWithoutBreak(g).iterator().next();
        if (!StringUtils.isEmpty(meshId)) {
            face.setMeshId(Integer.parseInt(meshId));
        }
        face.setArea(GeometryUtils.getCalculateArea(g));
        face.setPerimeter(GeometryUtils.getLinkLength(g));
        result.insertObject(face, ObjStatus.INSERT, face.getPid());
    }

    /*
     * 更新面的几何属性
     */
    private void updateGeometry(Geometry g, ZoneFace face) throws Exception {

        JSONObject updateContent = new JSONObject();
        g = GeoTranslator.transform(g, 0.00001, 5);
        updateContent.put("geometry", GeoTranslator.jts2Geojson(g));
        updateContent.put("area", GeometryUtils.getCalculateArea(g));
        updateContent.put("perimeter", GeometryUtils.getLinkLength(g));
        face.fillChangeFields(updateContent);
        result.insertObject(face, ObjStatus.UPDATE, face.getPid());
    }

    /*
     * 更新面的几何属性
     */
    private void createFace() throws Exception {
        ZoneFace face = new ZoneFace();
        face.setPid(PidUtil.getInstance().applyAdFacePid());
        result.setPrimaryPid(face.getPid());
        this.face = face;
    }

    /*
     * 重新维护faceTopo的顺序关系
     */
    private void reverseFaceTopo() {
        int newIndex = 0;
        for (int i = result.getAddObjects().size() - 1; i >= 0; i--) {
            if (result.getAddObjects().get(i) instanceof ZoneFaceTopo) {
                newIndex++;
                ((ZoneFaceTopo) result.getAddObjects().get(i)).setSeqNum(newIndex);

            }
        }
    }

}
