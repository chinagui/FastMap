package com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode;

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
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.RdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

    private Command command;

    private RdNode updateNode;

    private Connection conn;

    public Operation(Command command, RdNode updateNode, Connection conn) {
        this.command = command;

        this.updateNode = updateNode;

        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

        result.setPrimaryPid(updateNode.getPid());

        this.updateNodeGeometry(result);

        this.updateLinkGeomtry(result);

        return null;
    }

    private void updateLinkGeomtry(Result result) throws Exception {
        for (RdLink link : command.getLinks()) {

            Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);

            Coordinate[] cs = geom.getCoordinates();

            double[][] ps = new double[cs.length][2];

            for (int i = 0; i < cs.length; i++) {
                ps[i][0] = cs[i].x;

                ps[i][1] = cs[i].y;
            }

            if (link.getsNodePid() == command.getNodePid()) {
                ps[0][0] = command.getLongitude();

                ps[0][1] = command.getLatitude();
            } else {
                ps[ps.length - 1][0] = command.getLongitude();

                ps[ps.length - 1][1] = command.getLatitude();
            }

            JSONObject geojson = new JSONObject();

            geojson.put("type", "LineString");

            geojson.put("coordinates", ps);

            Geometry geo = GeoTranslator.geojson2Jts(geojson, 1, 5);

            Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
            // 修改线的几何属性
            // 如果没有跨图幅只是修改线的几何
            List<RdLink> links = new ArrayList<RdLink>();
            // 由于使用时原线的几何还没有进行更新，创建新的集合用于存放修改过geometry的link
            List<RdLink> newGeoLinks = new ArrayList<RdLink>();
            if (meshes.size() == 1) {
                JSONObject updateContent = new JSONObject();
                updateContent.put("geometry", geojson);
                updateContent.put("length", GeometryUtils.getLinkLength(geo));
                link.fillChangeFields(updateContent);

                // 添加修改过几何的RDLINK，该集合用于修改关联要素
                RdLink geoLink = new RdLink();
                geoLink.setPid(link.getPid());
                geoLink.copy(link);

                Geometry tmpGeo = GeoTranslator.geojson2Jts(geojson, 100000, 5);
                geoLink.setGeometry(tmpGeo);
                newGeoLinks.add(geoLink);

                links.add(link);
                result.insertObject(link, ObjStatus.UPDATE, link.pid());
                // 如果跨图幅就需要打断生成新的link
            } else {
                Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
                maps.put(geo.getCoordinates()[0], link.getsNodePid());
                maps.put(geo.getCoordinates()[geo.getCoordinates().length - 1], link.geteNodePid());
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

                                RdLinkOperateUtils.createRdLinkWithMesh(subGeo, maps, link, result, links);
                            }
                        }
                    } else {
                        geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);

                        RdLinkOperateUtils.createRdLinkWithMesh(geomInter, maps, link, result, links);
                    }

                }
                // 添加新生成的RDLINK的集合，该集合用于修改关联要素
                newGeoLinks.addAll(links);

                result.insertObject(link, ObjStatus.DELETE, link.pid());
            }

            updataRelationObj(link, newGeoLinks, result);
        }
    }

    private void updateNodeGeometry(Result result) throws Exception {
        JSONObject geojson = new JSONObject();

        String meshes[] = MeshUtils.point2Meshes(command.getLongitude(), command.getLatitude());

        Set<String> meshSet = new HashSet<String>(Arrays.asList(meshes));

        boolean isChangeMesh = false;

        for (IRow row : updateNode.getMeshes()) {
            RdNodeMesh nodeMesh = (RdNodeMesh) row;

            if (!meshSet.contains(String.valueOf(nodeMesh.getMeshId()))) {
                isChangeMesh = true;
                break;
            }
        }
        //图幅号发生改变后更新图幅号：先删除，后新增
        if (isChangeMesh) {
            for (IRow row : updateNode.getMeshes()) {
                RdNodeMesh nodeMesh = (RdNodeMesh) row;

                result.insertObject(nodeMesh, ObjStatus.DELETE, updateNode.getPid());
            }

            for (String mesh : meshes) {

                RdNodeMesh nodeMesh = new RdNodeMesh();
                nodeMesh.setNodePid(updateNode.getPid());
                nodeMesh.setMeshId(Integer.parseInt(mesh));

                result.insertObject(nodeMesh, ObjStatus.INSERT, updateNode.getPid());
            }
        }

        geojson.put("type", "Point");

        geojson.put("coordinates", new double[]{command.getLongitude(), command.getLatitude()});

        JSONObject updateContent = new JSONObject();

        // 要移动点的dbId
        updateContent.put("dbId", command.getDbId());
        JSONObject data = new JSONObject();
        // 移动点的新几何
        data.put("geometry", geojson);
        data.put("pid", updateNode.pid());
        data.put("objStatus", ObjStatus.UPDATE);
        updateContent.put("data", data);

        // 组装更新线的参数
        // 保证是同一个连接
        com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command updatecommand = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command(
                updateContent, command.getRequester(), this.command.getNode());
        com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process(
                updatecommand, result, conn);
        process.innerRun();
    }

    /**
     * 维护关联要素
     *
     * @throws Exception
     */
    private void updataRelationObj(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

        CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

        List<RdLink> sortLinks = calLinkOperateUtils.sortLink(newLinks);
        /*
         * 任何情况均需要处理的元素
		 */
        // 电子眼
        com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Operation eleceyeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Operation(
                this.conn);
        eleceyeOperation.moveEleceye(oldLink, newLinks, result);

        // poi被动维护（引导link，方位）
        com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation poiUpdateOption = new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Operation(
                this.conn);
        poiUpdateOption.updateLinkSideForPoi(oldLink, newLinks, result);

        // 维护限高限重
        com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Operation hgwgOperation = new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Operation(conn);
        hgwgOperation.moveHgwgLimit(oldLink, newLinks, result);

        // 同一点关系
        JSONObject updateJson = this.command.getJson();

        if (updateJson.containsKey("mainType")) {
            String mainType = updateJson.getString("mainType");

            if (mainType.equals(ObjType.LUNODE.toString())) {
                com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
                        null, this.conn);
                sameNodeOperation.moveMainNodeForTopo(this.command.getJson(), ObjType.RDNODE, result);
            }
        } else {
            com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation sameNodeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Operation(
                    null, this.conn);
            sameNodeOperation.moveMainNodeForTopo(this.command.getJson(), ObjType.RDNODE, result);
        }

        // 维护里程桩
        com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move.Operation maileageOperation = new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move.Operation(conn);
        maileageOperation.moveMileagepile(oldLink, newLinks, result);

        // 维护点限速
        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.move.Operation speedOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.move.Operation(conn);
        speedOperation.moveSpeedlimit(oldLink, newLinks, result);

		/*
		 * 条件以下为仅打断情况下需要处理的元素 (size < 2说明没有进行打断操作)
		 */
        if (newLinks.size() < 2) {
            return;
        }

        // 警示信息
        com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation warninginOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation(
                this.conn);
        warninginOperation.breakRdLink(null, oldLink.getPid(), newLinks, result);

        // 维护信号灯
        com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation trafficSignalOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation(
                this.conn);
        trafficSignalOperation.breakRdLink(null, oldLink.getPid(), newLinks, result);

        // 分岔路提示
        com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation rdSeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation(
                this.conn);
        rdSeOperation.breakRdSe(result, oldLink.pid(), newLinks);

        // 减速带
        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation rdSpeedbumpOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation(
                this.conn);
        rdSpeedbumpOpeartion.breakSpeedbump(null, result, oldLink.getPid(), newLinks);

        // 坡度
        com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation rdSlopeOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation(
                this.conn);
        rdSlopeOpeartion.breakRdLink(null, oldLink.getPid(), newLinks, result);

        // 顺行
        com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
                conn);
        operation.breakRdLink(oldLink, sortLinks, result);

        // 维护CRF交叉点
        com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Operation(
                this.conn);
        rdinterOperation.breakRdLink(oldLink, newLinks, result);

        // 维护CRF道路
        com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation rdRoadOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Operation(
                this.conn);
        rdRoadOperation.breakRdLink(oldLink.getPid(), newLinks, result);

        // 维护CRF对象
        com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation rdObjectOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
                this.conn);
        rdObjectOperation.breakRdObjectLink(oldLink, newLinks, result);

        // 收费站
        com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation rdTollgateOpeartion = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation(
                this.conn);
        rdTollgateOpeartion.breakRdTollgate(result, oldLink.getPid(), newLinks);

        // 语音引导
        com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation voiceguideOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation(
                conn);
        voiceguideOperation.breakRdLink(oldLink, sortLinks, result);

        // 维护可变限速关系
        com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation variableSpeedOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation(
                this.conn);
        variableSpeedOperation.breakLine(null, oldLink, newLinks, result);
        // 详细车道维护
        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation rdlaneOperation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(this.conn);
        rdlaneOperation.breakRdLink(oldLink, newLinks, result);

        //tmc维护
        com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation rdTmcLocationLinkOperation = new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation(
                this.conn);
        rdTmcLocationLinkOperation.breakLinkUpdateTmc(result, oldLink, newLinks);

    }
}
