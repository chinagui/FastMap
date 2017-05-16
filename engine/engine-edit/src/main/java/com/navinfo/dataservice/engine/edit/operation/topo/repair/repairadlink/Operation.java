package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;

import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;

import com.navinfo.dataservice.engine.edit.utils.AdLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

    private Command command;

    private Connection conn;
    private Map<Integer, List<AdLink>> map;

    public Map<Integer, List<AdLink>> getMap() {
        return map;
    }

    public void setMap(Map<Integer, List<AdLink>> map) {
        this.map = map;
    }

    public Operation(Connection conn, Command command) {

        this.conn = conn;

        this.command = command;

    }

    @Override
    public String run(Result result) throws Exception {
        // 处理修行挂接功能
        this.caleCatchs(result);
        // 修行修改线信息
        this.updateLink(result);
        // 修行修改面信息
        this.updateFace(result);
        return null;
    }

    private Check check =new Check();
    
    /***
     * 修行挂接点和线
     *
     * @param result
     * @throws Exception
     */
    private void caleCatchs(Result result) throws Exception {
    	check.PERMIT_MODIFICATE_POLYGON_ENDPOINT(this.command, this.conn);
        if (this.command.getCatchInfos() != null && this.command.getCatchInfos().size() > 0) {
            AdNodeSelector nodeSelector = new AdNodeSelector(conn);
            AdLinkSelector linkSelector = new AdLinkSelector(conn);
            for (int i = 0; i < this.command.getCatchInfos().size(); i++) {
                JSONObject obj = this.command.getCatchInfos().getJSONObject(i);
                // 分离移动的node
                int nodePid = obj.getInt("nodePid");
                Point point = null;
                double lon = 0;
                double lat = 0;
                if (!obj.containsKey("catchNodePid")) {
                    point = (Point) GeoTranslator.transform(GeoTranslator.point2Jts(obj.getDouble("longitude"), obj.getDouble("latitude")), 1, 5);
                    // 分离移动后的经纬度
                    lon = point.getX();
                    lat = point.getY();
                }

                AdNode preNode = (AdNode) nodeSelector.loadById(nodePid, true, false);
                // 分离node挂接的link
                List<AdLink> links = linkSelector.loadByNodePid(nodePid, true);

                if (obj.containsKey("catchNodePid") && obj.getInt("catchNodePid") != 0) {
                    // 分离节点挂接功能
                    this.departCatchtNode(result, nodePid, obj.getInt("catchNodePid"), preNode, links);

                } else if (obj.containsKey("catchLinkPid") && obj.getInt("catchLinkPid") != 0) {
                    // 分离节点挂接打断功能
                    this.departCatchBreakLink(lon, lat, preNode, obj.getInt("catchLinkPid"), links, result);
                } else {
                    // 移动功能
                    if (links.size() == 1) {
                        this.moveNodeGeo(preNode, lon, lat, result);
                    } else {
                        this.departNode(result, nodePid, lon, lat);
                    }
                }

            }

        }

    }

    /***
     *
     * @param result
     * @param nodePid
     * @param lon
     * @param lat
     * @throws Exception
     */
    private void departNode(Result result, int nodePid, double lon, double lat) throws Exception {

        // 分离功能
        AdNode node = NodeOperateUtils.createAdNode(lon, lat);
        result.insertObject(node, ObjStatus.INSERT, node.pid());
        this.updateNodeForLink(nodePid, node.getPid());

    }

    /***
     * 分离节点 修行挂接Node操作
     *
     * @param result
     * @param preNode
     * @throws Exception
     */
    private void departCatchtNode(Result result, int nodePid, int catchNodePid, AdNode preNode, List<AdLink> links) throws Exception {
        AdNodeSelector nodeSelector = new AdNodeSelector(conn);
        // 用分离挂接的Node替换修行Link对应的几何,以保持精度
        AdNode catchNode = (AdNode) nodeSelector.loadById(catchNodePid, true, true);
        // 获取挂接Node的几乎额
        Geometry geom = GeoTranslator.transform(catchNode.getGeometry(), 0.00001, 5);
        Point point = (((Point) GeoTranslator.point2Jts(geom.getCoordinate().x, geom.getCoordinate().y)));
        // 如果原有node挂接的LINK<=1 原来的node需要删除更新link的几何为新的node
        if (links.size() <= 1) {
            result.insertObject(preNode, ObjStatus.DELETE, preNode.getPid());
        }
        // 更新link的几何为新的node点
        this.updateNodeForLink(nodePid, catchNodePid);
        // 更新link的几何用挂接的点的几何代替link的起始形状点
        if (this.command.getUpdateLink().getsNodePid() == nodePid) {

            this.command.getLinkGeom().getCoordinates()[0] = point.getCoordinate();
        } else {
            this.command.getLinkGeom().getCoordinates()[this.command.getLinkGeom().getCoordinates().length - 1] = point.getCoordinate();
        }
    }

    /***
     * 分离节点 挂接Link打断功能能
     *
     * @param lon
     *            打断点经度
     * @param lat
     *            打断点的维度
     * @param preNode
     *            分离的node
     * @param linkPid
     *            挂节点的linkPid
     * @param links
     *            分离node挂接的node
     * @param result
     * @throws Exception
     */
    private void departCatchBreakLink(double lon, double lat, AdNode preNode, int linkPid, List<AdLink> links, Result result) throws Exception {
        JSONObject breakJson = new JSONObject();
        breakJson.put("objId", linkPid);
        breakJson.put("dbId", command.getDbId());
        JSONObject data = new JSONObject();
        // 如果没有挂接的link node需要继承 如果有node需要新生成
        int breakNodePid = preNode.getPid();
        if (links.size() > 1) {
            AdNode node = NodeOperateUtils.createAdNode(lon, lat);
            result.insertObject(node, ObjStatus.INSERT, node.getPid());
            breakNodePid = node.getPid();
            this.updateNodeForLink(preNode.getPid(), breakNodePid);

        }
        // node继承需要修改node的几何
        else {
            this.moveNodeGeo(preNode, lon, lat, result);

        }

        // 组装打断的参数
        data.put("longitude", lon);
        data.put("latitude", lat);
        data.put("breakNodePid", breakNodePid);
        breakJson.put("data", data);
        // 调用打断的API
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(breakJson, breakJson.toString());
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(breakCommand, result, conn);
        breakProcess.innerRun();

    }

    /***
     *
     *
     * @param node
     *            移动点的对象
     * @param lon
     *            移动后的经度
     * @param lat
     *            移动后的纬度
     * @param result
     * @throws Exception
     */
    private void moveNodeGeo(AdNode node, double lon, double lat, Result result) throws Exception {
        JSONObject geojson = new JSONObject();
        geojson.put("type", "Point");
        geojson.put("coordinates", new double[]{lon, lat});
        JSONObject updateContent = new JSONObject();
        // 要移动点的dbId
        updateContent.put("dbId", command.getDbId());
        JSONObject data = new JSONObject();
        // 移动点的新几何
        data.put("geometry", geojson);
        data.put("pid", node.getPid());
        data.put("objStatus", ObjStatus.UPDATE);
        updateContent.put("data", data);
        // 组装更新node的参数
        // 保证是同一个连接
        com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command(updateContent, command.getRequester(), node);
        com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process(updatecommand, result, conn);
        process.innerRun();

    }

    /***
     * 重新赋值link的起始点的pid
     *
     * @param nodePid
     *            原始link的端点pid
     * @param pid
     *            修行后新的端点pid
     * @throws Exception
     */
    private void updateNodeForLink(int nodePid, int pid) throws Exception {
        JSONObject content = new JSONObject();
        if (this.command.getUpdateLink().getsNodePid() == nodePid) {
            content.put("sNodePid", pid);
            this.command.getUpdateLink().fillChangeFields(content);

        } else {
            content.put("eNodePid", pid);
            this.command.getUpdateLink().fillChangeFields(content);
        }
    }

    /**
     * 修改线的信息
     *
     * @param result
     * @throws Exception
     */
    private void updateLink(Result result) throws Exception {
        Map<Integer, List<AdLink>> map = new HashMap<Integer, List<AdLink>>();
        List<AdLink> links = new ArrayList<AdLink>();
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(command.getLinkGeom());
        if (meshes.size() == 1) {
            JSONObject content = new JSONObject();
            result.setPrimaryPid(this.command.getUpdateLink().getPid());

            Geometry geo = command.getLinkGeom();

            if (this.command.getOperationType().equals("sameLinkRepair")) {
                Coordinate oldGeoSCoordinate = this.command.getUpdateLink().getGeometry().getCoordinate();

                Coordinate newGeoSCoordinate = command.getLinkGeom().getCoordinate();
                if (!GeoTranslator.isPointEquals(oldGeoSCoordinate, newGeoSCoordinate)) {
                    geo = command.getLinkGeom().reverse();
                }

            }

            content.put("geometry", GeoTranslator.jts2Geojson(geo));

            double length = 0;
            if (null != geo)
                length = GeometryUtils.getLinkLength(geo);
            content.put("length", length);
            boolean isChanged = this.command.getUpdateLink().fillChangeFields(content);
            AdLink adLink = new AdLink();
            adLink.setPid(this.command.getUpdateLink().getPid());
            adLink.copy(this.command.getUpdateLink());
            if (isChanged) {
                result.insertObject(this.command.getUpdateLink(), ObjStatus.UPDATE, this.command.getLinkPid());
                adLink.setGeometry(GeoTranslator.transform(command.getLinkGeom(), 100000, 0));
            }
            links.add(adLink);
        } else {
            Iterator<String> it = meshes.iterator();
            Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
            Geometry g = GeoTranslator.transform(this.command.getUpdateLink().getGeometry(), 0.00001, 5);
            maps.put(g.getCoordinates()[0], this.command.getUpdateLink().getsNodePid());
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], this.command.getUpdateLink().geteNodePid());
            List<String> geoList = new ArrayList<>();
            while (it.hasNext()) {
                String meshIdStr = it.next();
                Geometry geomInter = MeshUtils.linkInterMeshPolygon(command.getLinkGeom(), GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr), 1, 5));
                if (geomInter instanceof GeometryCollection) {
                    int geoNum = geomInter.getNumGeometries();
                    for (int i = 0; i < geoNum; i++) {
                        Geometry subGeo = geomInter.getGeometryN(i);
                        if (subGeo instanceof LineString) {
                            subGeo = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(subGeo), 1, 5);

                            links.addAll(AdLinkOperateUtils.getCreateAdLinksWithMesh(subGeo, maps, result, this.command.getUpdateLink()));
                        }
                    }
                } else {
                    geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);
                    // 创建图幅覆盖线时防止重复创建
                    if (geoList.contains(geomInter.toString()) || geoList.contains(geomInter.reverse().toString()))
                        continue;
                    else
                        geoList.add(geomInter.toString());
                    links.addAll(AdLinkOperateUtils.getCreateAdLinksWithMesh(geomInter, maps, result, this.command.getUpdateLink()));
                }
            }
            result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE, this.command.getUpdateLink().getPid());
        }
        map.put(this.command.getLinkPid(), links);

        updataRelationObj(links, result);

        this.map = map;
    }

    /**
     * 修改面的信息
     *
     * @param result
     * @throws Exception
     */
    private void updateFace(Result result) throws Exception {
        if (command.getFaces() != null && command.getFaces().size() > 0) {
            for (AdFace face : command.getFaces()) {
                boolean flag = false;
                List<AdLink> links = new ArrayList<AdLink>();
                for (IRow iRow : face.getFaceTopos()) {
                    AdFaceTopo obj = (AdFaceTopo) iRow;
                    if (this.map.containsKey(obj.getLinkPid())) {
                        if (this.map.get(obj.getLinkPid()).size() > 1) {
                            flag = true;
                        }
                        links.addAll(this.map.get(obj.getLinkPid()));
                    } else {
                        links.add((AdLink) new AdLinkSelector(conn).loadById(obj.getLinkPid(), true));
                    }

                    result.insertObject(obj, ObjStatus.DELETE, face.getPid());

                }
                if (flag) {
                    // 如果跨图幅需要重新生成面并且删除原有面信息
                    com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation(conn, result);
                    List<IObj> objs = new ArrayList<IObj>();
                    objs.addAll(links);
                    opFace.createFaceByAdLink(objs);
                    result.insertObject(face, ObjStatus.DELETE, face.getPid());
                } else {
                    // 如果不跨图幅只需要维护面的行政几何
                    com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation(conn, result, face);
                    opFace.reCaleFaceGeometry(links);
                }
            }

        }
    }

    /**
     * 维护关联要素
     *
     * @throws Exception
     */
    private void updataRelationObj(List<AdLink> links, Result result) throws Exception {

        if (links.size() == 1) {
            if (!this.command.getOperationType().equals("sameLinkRepair")) {
                // 维护同一线
                com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation samelinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(this.conn);

                samelinkOperation.repairLink(links.get(0), this.command.getRequester(), result);
            }
        }
    }

}
