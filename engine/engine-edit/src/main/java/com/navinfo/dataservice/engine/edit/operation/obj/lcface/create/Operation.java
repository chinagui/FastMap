package com.navinfo.dataservice.engine.edit.operation.obj.lcface.create;

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
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.*;
import com.navinfo.dataservice.engine.edit.utils.LcLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

    private Command command;
    private Check check;
    private Connection conn;
    private Result result;
    private LcFace face;
    private boolean updateFlag = true;

    public Operation(Result result) {
        this.result = result;
    }

    public Operation(Result result, LcFace face) {
        this.face = face;
        this.result = result;
    }

    public Operation(Command command, Check check, Connection conn, Result result) {
        this.command = command;

        this.check = check;

        this.conn = conn;
        this.result = result;
        this.updateFlag = false;
    }

    @Override
    public String run(Result result) throws Exception {

        // 既有线构成面
        if (command.getLinkPids() != null) {
            this.createFaceByLcLink(command.getLinks(), null);
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
    public void createFaceByLcLink(List<IObj> objList, LcFace face) throws Exception {
        Set<String> meshes = new HashSet<String>();
        List<LcLink> lcLinks = new ArrayList<LcLink>();
        for (IObj obj : objList) {
            LcLink link = (LcLink) obj;
            lcLinks.add(link);
            if (link.getMeshes().size() == 1) {
                for (IRow iRow : link.getMeshes()) {
                    LcLinkMesh lclinkmesh = (LcLinkMesh) iRow;
                    meshes.add(String.valueOf(lclinkmesh.getMeshId()));
                }
            }
        }
        if (meshes.size() == 1) {
            int meshId = Integer.parseInt(meshes.iterator().next());
            this.createFace(face);
            this.face.setMeshId(meshId);
            this.face.setMesh(meshId);
            this.reCaleFaceGeometry(lcLinks);
        } else {
            this.updateFlag = false;
            Geometry geom = GeoTranslator.transform(this.getPolygonGeometry(lcLinks), 0.00001, 5);
            this.createFaceWithMesh(meshes, geom, objList, 1, face);
        }
    }

    /**
     * @param meshes 跨域图幅
     * @param geom   初始画面几何
     * @param flag   创建面表示 0 根据几何，1 根据既有线
     * @throws Exception
     */
    private void createFaceWithMesh(Set<String> meshes, Geometry geom, List<IObj> objList, int flag, LcFace face) throws Exception {
        Iterator<String> it = meshes.iterator();
        Map<Coordinate, Integer> mapNode = new HashMap<Coordinate, Integer>();
        Map<Geometry, LcLink> mapLink = new HashMap<Geometry, LcLink>();
        if (flag == 1) {
            for (IObj obj : objList) {
                LcLink lcLink = (LcLink) obj;
                Geometry geometry = GeoTranslator.transform(lcLink.getGeometry(), 0.00001, 5);
                mapLink.put(geometry, lcLink);

                if (!mapNode.containsKey(geometry.getCoordinates()[0])) {
                    mapNode.put(geometry.getCoordinates()[0], lcLink.getsNodePid());
                }
                if (!mapNode.containsKey(geometry.getCoordinates()[geometry.getCoordinates().length - 1])) {
                    mapNode.put(geometry.getCoordinates()[geometry.getCoordinates().length - 1], lcLink.geteNodePid());
                }

            }
        }
        while (it.hasNext()) {
            String meshIdStr = it.next();
            // 获取每个图幅中闭合线的数组
            Set<LineString[]> set = CompGeometryUtil.cut(JtsGeometryFactory.createPolygon(geom.getCoordinates()),
                    meshIdStr);
            Iterator<LineString[]> itLine = set.iterator();
            while (itLine.hasNext()) {
                LineString[] lineStrings = itLine.next();
                List<LcLink> links = new ArrayList<LcLink>();
                for (LineString lineString : lineStrings) {
                    LcLink lcLink = null;
                    if (MeshUtils.isMeshLine(lineString)) {
                        if (mapLink.containsKey(lineString.reverse())) {
                            lcLink = mapLink.get(lineString.reverse());
                        } else if (mapLink.containsKey(lineString)) {
                            lcLink = mapLink.get(lineString);
                        } else {
                            lcLink = this.createLinkOfFace(lineString, mapNode);
                            mapLink.put(lineString, lcLink);
                        }
                        links.add(lcLink);

                    } else {
                        if (flag == 0) {
                            if (mapLink.containsKey(lineString)) {
                                lcLink = mapLink.get(lineString);
                            } else {
                                lcLink = this.createLinkOfFace(lineString, mapNode);
                                mapLink.put(lineString, lcLink);
                            }
                            links.add(lcLink);

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
                this.createFace(face);
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
            LcNode Node = NodeOperateUtils.createLcNode(sPoint.x, sPoint.y);
            result.insertObject(Node, ObjStatus.INSERT, Node.pid());
            this.createFace(null);
            List<LcLink> links = new ArrayList<LcLink>();
            LcLink lcLink = LcLinkOperateUtils.getAddLink(geom, Node.getPid(), Node.getPid(), result, null);
            links.add(lcLink);
            LcLinkKind kind = new LcLinkKind();
            kind.setLinkPid(lcLink.pid());
            lcLink.getKinds().add(kind);
            this.reCaleFaceGeometry(links);
        } // 如果跨图幅
        else {
            this.createFaceWithMesh(meshes, geom, null, 0, null);
        }

    }

    private LcLink createLinkOfFace(Geometry g, Map<Coordinate, Integer> maps) throws Exception {
        int sNodePid = 0;
        int eNodePid = 0;
        if (maps.containsKey(g.getCoordinates()[0])) {
            sNodePid = maps.get(g.getCoordinates()[0]);
        }
        if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
            eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
        }
        JSONObject node = LcLinkOperateUtils.createLcNodeForLink(g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        LcLink lcLink = LcLinkOperateUtils.getAddLink(g, (int) node.get("s"), (int) node.get("e"), result, null);
        LcLinkKind kind = new LcLinkKind();
        kind.setLinkPid(lcLink.pid());
        lcLink.getKinds().add(kind);
        return lcLink;
    }

    /*
     * 添加Link和FaceTopo关系
     */
    public void lcdLink(Map<LcLink, Integer> map) {
        List<IRow> lcFaceTopos = new ArrayList<IRow>();
        for (LcLink link : map.keySet()) {
            LcFaceTopo faceTopo = new LcFaceTopo();
            faceTopo.setLinkPid(link.getPid());
            faceTopo.setFacePid(face.getPid());
            faceTopo.setSeqNum(map.get(link));
            lcFaceTopos.add(faceTopo);
        }
        this.face.setTopos(lcFaceTopos);
    }

    /*
     * 添加Link和FaceTopo关系
     */
    public void createFaceTop(Map<LcLink, Integer> map) {
        List<IRow> lcFaceTopos = new ArrayList<IRow>();
        for (LcLink link : map.keySet()) {
            LcFaceTopo faceTopo = new LcFaceTopo();
            faceTopo.setLinkPid(link.getPid());
            faceTopo.setFacePid(face.getPid());
            faceTopo.setSeqNum(map.get(link));
            lcFaceTopos.add(faceTopo);
            result.insertObject(faceTopo, ObjStatus.INSERT, face.getPid());
        }

    }

    /*
     * @param List 按照LCFACE的形状重新维护LCFACE
     */
    public void reCaleFaceGeometry(List<LcLink> links) throws Exception {
        if (links.size() < 1) {
            throw new Exception("重新维护面的形状:发现面没有组成link");
        }
        LcLink currLink = null;
        for (LcLink lcLink : links) {
            currLink = lcLink;
            break;
        }
        if (currLink == null) {
            return;
        }
        // 获取当前LINK和NODE
        int startNodePid = currLink.getsNodePid();
        int currNodePid = startNodePid;
        Map<LcLink, Integer> map = new HashMap<LcLink, Integer>();
        map.put(currLink, 1);
        int index = 1;
        List<Geometry> list = new ArrayList<Geometry>();
        list.add(currLink.getGeometry());
        Map<Integer, LcLink> currLinkAndPidMap = new HashMap<Integer, LcLink>();
        currLinkAndPidMap.put(currNodePid, currLink);
        // 获取下一条联通的LINK
        while (LcLinkOperateUtils.getNextLink(links, currLinkAndPidMap)) {
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
            this.lcdLink(map);
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
        // 更新面的几何属性
        if (this.updateFlag) {
            this.updateGeometry(GeoTranslator.getPolygonToPoints(c1), this.face);

        } else {
            this.createFaceGeometry(GeoTranslator.getPolygonToPoints(c1), this.face);
        }

    }

    /**
     * 根据传入的link 重组PolygonGeometry
     */
    private Geometry getPolygonGeometry(List<LcLink> links) throws Exception {
        if (links.size() < 1) {
            throw new Exception("重新维护面的形状:发现面没有组成link");
        }
        LcLink currLink = null;
        for (LcLink lcLink : links) {
            currLink = lcLink;
            break;
        }
        if (currLink == null) {
            throw new Exception("重新维护面的形状:发现面没有组成link");
        }
        // 获取当前LINK和NODE
        int startNodePid = currLink.getsNodePid();
        int currNodePid = startNodePid;
        Map<LcLink, Integer> map = new HashMap<LcLink, Integer>();
        map.put(currLink, 1);
        List<Geometry> list = new ArrayList<Geometry>();
        list.add(currLink.getGeometry());
        Map<Integer, LcLink> currLinkAndPidMap = new HashMap<Integer, LcLink>();
        currLinkAndPidMap.put(currNodePid, currLink);
        // 获取下一条联通的LINK
        while (LcLinkOperateUtils.getNextLink(links, currLinkAndPidMap)) {
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
    private void createFaceGeometry(Geometry g, LcFace face) throws Exception {
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
        result.setPrimaryPid(face.pid());
    }

    /*
     * 更新面的几何属性
     */
    private void updateGeometry(Geometry g, LcFace face) throws Exception {

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
    private void createFace(LcFace f) throws Exception {
        LcFace face = new LcFace();
        face.setPid(PidUtil.getInstance().applyAdFacePid());
        result.setPrimaryPid(face.getPid());
        if (null != f) {
            face.setFeaturePid(f.getFeaturePid());
            face.setKind(f.getKind());
            face.setForm(f.getForm());
            face.setDisplayClass(f.getDisplayClass());
            face.setDetailFlag(f.getDetailFlag());
            face.setScale(f.getScale());
        }
        this.face = face;
    }

    /*
     * 重新维护faceTopo的顺序关系
     */
    private void reverseFaceTopo() {
        int newIndex = 0;
        for (int i = result.getAddObjects().size() - 1; i >= 0; i--) {
            if (result.getAddObjects().get(i) instanceof LcFaceTopo) {
                newIndex++;
                ((LcFaceTopo) result.getAddObjects().get(i)).setSeqNum(newIndex);

            }
        }
    }

}
