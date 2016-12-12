package com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.*;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.AdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONObject;

/**
 * @author zhaokk 移动行政区划点操作类 移动行政区划点 点不会打断其它的行政区划线
 */
public class Operation implements IOperation {

    private Command command;

    private AdNode updateNode;
    private Map<Integer, List<AdLink>> map;
    private Connection conn;

    public Operation(Command command, AdNode updateNode, Connection conn) {
        this.command = command;
        this.updateNode = updateNode;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        result.setPrimaryPid(updateNode.getPid());
        this.updateNodeGeometry(result);
        this.updateLinkGeomtry(result);
        this.updateFaceGeomtry(result);
        return null;
    }

    /*
     * 移动行政区划点修改对应的线的信息
     */
    private void updateLinkGeomtry(Result result) throws Exception {
        Map<Integer, List<AdLink>> map = new HashMap<Integer, List<AdLink>>();
        for (AdLink link : command.getLinks()) {
            int nodePid = updateNode.pid();

            double lon = command.getLongitude();

            double lat = command.getLatitude();

            Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
            Coordinate[] cs = geom.getCoordinates();
            double[][] ps = new double[cs.length][2];

            for (int i = 0; i < cs.length; i++) {
                ps[i][0] = cs[i].x;

                ps[i][1] = cs[i].y;
            }

            if (link.getsNodePid() == nodePid) {
                ps[0][0] = lon;

                ps[0][1] = lat;
            }
            if (link.geteNodePid() == nodePid) {
                ps[ps.length - 1][0] = lon;

                ps[ps.length - 1][1] = lat;
            }
            JSONObject geojson = new JSONObject();
            geojson.put("type", "LineString");
            geojson.put("coordinates", ps);
            Geometry geo = GeoTranslator.geojson2Jts(geojson, 1, 5);
            Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
            // 修改线的几何属性
            // 如果没有跨图幅只是修改线的几何
            List<AdLink> links = new ArrayList<AdLink>();
            List<String> linkMeshes = new ArrayList<String>();
            for (IRow row : link.getMeshes()) {
                linkMeshes.add(((AdLinkMesh) row).getMeshId() + "");
            }
            if (linkMeshes.containsAll(meshes)) {
                JSONObject updateContent = new JSONObject();
                updateContent.put("geometry", geojson);
                updateContent.put("length", GeometryUtils.getLinkLength(geo));
                link.fillChangeFields(updateContent);
                AdLink adLink = new AdLink();
                adLink.setPid(link.getPid());
                adLink.copy(link);
                adLink.setGeometry(GeoTranslator.geojson2Jts(geojson, 100000, 5));
                links.add(adLink);
                map.put(link.getPid(), links);
                result.insertObject(link, ObjStatus.UPDATE, link.pid());
                // 如果跨图幅就需要打断生成新的link
            } else {
                Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
                maps.put(geo.getCoordinates()[0], link.getsNodePid());
                maps.put(geo.getCoordinates()[link.getGeometry().getCoordinates().length - 1], link.geteNodePid());
                Iterator<String> it = meshes.iterator();
                while (it.hasNext()) {
                    String meshIdStr = it.next();
                    Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo, GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr), 1, 5));
                    if (geomInter instanceof GeometryCollection) {
                        int geoNum = geomInter.getNumGeometries();
                        for (int i = 0; i < geoNum; i++) {
                            Geometry subGeo = geomInter.getGeometryN(i);
                            if (subGeo instanceof LineString) {
                                subGeo = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(subGeo), 1, 5);

                                links.addAll(AdLinkOperateUtils.getCreateAdLinksWithMesh(subGeo, maps, result, link));
                            }
                        }
                    } else {
                        geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);

                        links.addAll(AdLinkOperateUtils.getCreateAdLinksWithMesh(geomInter, maps, result, link));
                    }
                }
                map.put(link.getPid(), links);
                result.insertObject(link, ObjStatus.DELETE, link.pid());
            }
            updataRelationObj(link, links, result);
        }
        this.map = map;
    }

    /**
     * @param link
     * @param links
     * @param result
     * @throws Exception
     */
    private void updataRelationObj(AdLink link, List<AdLink> links, Result result) throws Exception {
        // 同一点关系
        JSONObject updateJson = this.command.getJson();

        if (updateJson.containsKey("mainType")) {
            String mainType = updateJson.getString("mainType");

            if (mainType.equals(ObjType.ADNODE.toString())) {
                com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
                        null, this.conn);
                sameNodeOperation.moveMainNodeForTopo(updateJson, ObjType.ADNODE, result);
            }
        } else {
            com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
                    null, this.conn);
            sameNodeOperation.moveMainNodeForTopo(updateJson, ObjType.ADNODE, result);
        }

    }

    /*
     * 移动行政区划点修改对应的点的信息
     */
    private void updateNodeGeometry(Result result) throws Exception {

        String meshes[] = MeshUtils.point2Meshes(command.getLongitude(), command.getLatitude());

        Set<String> meshSet = new HashSet<String>(Arrays.asList(meshes));

        boolean isChangeMesh = false;

        for (IRow row : updateNode.getMeshes()) {
            AdNodeMesh nodeMesh = (AdNodeMesh) row;


            if (!meshSet.contains(String.valueOf(nodeMesh.getMeshId()))) {
                isChangeMesh = true;
                break;
            }
        }
        //图幅号发生改变后更新图幅号：先删除，后新增
        if (isChangeMesh) {
            for (IRow row : updateNode.getMeshes()) {
                AdNodeMesh nodeMesh = (AdNodeMesh) row;

                result.insertObject(nodeMesh, ObjStatus.DELETE, updateNode.getPid());
            }

            for (String mesh : meshes) {

                AdNodeMesh nodeMesh = new AdNodeMesh();
                nodeMesh.setNodePid(updateNode.getPid());
                nodeMesh.setMeshId(Integer.parseInt(mesh));

                result.insertObject(nodeMesh, ObjStatus.INSERT, updateNode.getPid());
            }
        }

        // 计算点的几何形状
        JSONObject geojson = new JSONObject();
        geojson.put("type", "Point");
        geojson.put("coordinates", new double[]{command.getLongitude(), command.getLatitude()});
        JSONObject updateNodeJson = new JSONObject();
        // 要移动点的project_id
        updateNodeJson.put("dbId", command.getDbId());
        JSONObject data = new JSONObject();
        // 移动点的新几何
        data.put("geometry", geojson);
        data.put("pid", updateNode.pid());
        data.put("objStatus", ObjStatus.UPDATE);
        updateNodeJson.put("data", data);

        // 组装打断线的参数
        // 保证是同一个连接
        com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command(
                updateNodeJson, command.getRequester());
        com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process(
                updatecommand, result, conn);
        process.innerRun();
    }

    /**
     * 移动Adnode 修改行政区划面信息
     *
     * @param result
     * @throws Exception
     */
    private void updateFaceGeomtry(Result result) throws Exception {
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
                    com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation(
                            result);
                    List<IObj> objs = new ArrayList<IObj>();
                    objs.addAll(links);
                    opFace.createFaceByAdLink(objs);
                    result.insertObject(face, ObjStatus.DELETE, face.getPid());
                } else {
                    // 如果不跨图幅只需要维护面的行政几何
                    com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Operation(
                            result, face);
                    opFace.reCaleFaceGeometry(links);
                }

            }
        }

    }
}