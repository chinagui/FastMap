package com.navinfo.dataservice.engine.edit.operation.obj.lclink.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.BasicServiceUtils;
import com.navinfo.dataservice.engine.edit.utils.LcLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Check check, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        String msg = null;
        Map<Geometry, JSONObject> map = new HashMap<Geometry, JSONObject>();
        // 如果创建土地覆盖线有对应的挂接LCNODE和LCFACE
        // 执行挂接线处理逻辑
        if (command.getCatchLinks().size() > 0) {
            this.caleCatchModifyLcLink();
            this.initCommandPara();
            map = LcLinkOperateUtils.splitLink(command.getGeometry(), command.getsNodePid(), command.geteNodePid(),
                    command.getCatchLinks(), result);
        }
        // 如果创建土地覆盖线没有对应的挂接LCNODE和LCFACE
        // 创建对应的LCNODE
        if (command.getCatchLinks().size() == 0 || map.size() == 0) {
            JSONObject se = new JSONObject();
            se = LcLinkOperateUtils.createLcNodeForLink(command.getGeometry(), command.getsNodePid(), command
                    .geteNodePid(), result);
            map.put(command.getGeometry(), se);
        }
        // 创建土地覆盖线信息
        this.createLcLinks(map, result);
        // 挂接的线被打断的操作
        this.breakLine(result);

        return msg;
    }

    /***
     * 当前台未开启挂接功能是，如果传入的点正好是link的端点 应按照挂接node来传参数
     *
     * @throws Exception
     */
    private void caleCatchModifyLcLink() throws Exception {
        for (int i = 0; i < command.getCatchLinks().size(); i++) {
            JSONObject modifyJson = command.getCatchLinks().getJSONObject(i);
            if (modifyJson.containsKey("linkPid")) {
                LcLinkSelector linkSelector = new LcLinkSelector(conn);
                IRow row = linkSelector.loadById(modifyJson.getInt("linkPid"), false, true);
                LcLink link = (LcLink) row;
                Geometry geometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
                if (geometry.getCoordinates()[0].x == modifyJson.getDouble("lon")

                        && geometry.getCoordinates()[0].y == modifyJson

                        .getDouble("lat")) {
                    modifyJson.remove("linkPid");
                    modifyJson.put("nodePid", link.getsNodePid());

                }
                if (geometry.getCoordinates()[geometry.getCoordinates().length - 1].x == modifyJson.getDouble("lon")

                        && geometry.getCoordinates()[geometry.getCoordinates().length - 1].y == modifyJson

                        .getDouble("lat")) {
                    modifyJson.remove("linkPid");
                    modifyJson.put("nodePid", link.geteNodePid());

                }
            }
        }

    }

	/*
     * 创建多条被分割的线 1.按照线是否跨图幅逻辑走不同分支生成线
	 */

    public void createLcLinks(Map<Geometry, JSONObject> map, Result result) throws Exception {

        for (Geometry g : map.keySet()) {
            Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
            // 不跨图幅
            if (meshes.size() == 1) {
                this.createLcLinkWithNoMesh(g, (int) map.get(g).get("s"), (int) map.get(g).get("e"), result);
            }
            // 跨图幅
            else {
                Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
                maps.put(g.getCoordinates()[0], (int) map.get(g).get("s"));
                maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) map.get(g).get("e"));
                Iterator<String> it = meshes.iterator();
                List<String> geoList = new ArrayList<>();
                while (it.hasNext()) {
                    String meshIdStr = it.next();
                    Geometry geomInter = MeshUtils.linkInterMeshPolygon(g, GeoTranslator.transform(MeshUtils.mesh2Jts
                            (meshIdStr), 1, 5));
                    if (geomInter instanceof GeometryCollection) {
                        int geoNum = geomInter.getNumGeometries();
                        for (int i = 0; i < geoNum; i++) {
                            Geometry subGeo = geomInter.getGeometryN(i);
                            if (subGeo instanceof LineString) {
                                subGeo = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(subGeo), 1, 5);
                                LcLinkOperateUtils.createLcLinkWithMesh(subGeo, maps, result);
                            }
                        }
                    } else {
                        geomInter = GeoTranslator.geojson2Jts(GeoTranslator.jts2Geojson(geomInter), 1, 5);
                        if (geoList.contains(geomInter.toString()) || geoList.contains(geomInter.reverse().toString()))
                            continue;
                        else
                            geoList.add(geomInter.toString());
                        LcLinkOperateUtils.createLcLinkWithMesh(geomInter, maps, result);
                    }
                }
            }
        }
    }

    /*
     * 创建土地覆盖线 不跨图幅生成线
     */
    private void createLcLinkWithNoMesh(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
        if (g != null) {
            JSONObject node = LcLinkOperateUtils.createLcNodeForLink(g, sNodePid, eNodePid, result);
            LcLinkOperateUtils.addLink(g, (int) node.get("s"), (int) node.get("e"), result);
        }
    }

    /*
     * LC_LINK打断具体操作 1.循环挂接的线 2.如果有被打断操作执行打断功能
     */
    public void breakLine(Result result) throws Exception { // 处理连续打断参数

        JSONArray resultArr = BasicServiceUtils.getBreakArray(command.getCatchLinks());
        // 组装打断操作流程
        for (int i = 0; i < resultArr.size(); i++) {
            JSONObject obj = resultArr.getJSONObject(i);
            JSONObject breakJson = BasicServiceUtils.getBreaksPara(obj, this.command.getDbId());
            // 组装打断线的参数
            // 保证是同一个连接
            com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command breakCommand = new com
                    .navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command(breakJson, breakJson
                    .toString());
            com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process breakProcess = new com
                    .navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process(breakCommand,
                    result, conn);
            breakProcess.innerRun();
        }
    }

    /***
     * 新增link参数初始化 1.几何保留五位精度 2.捕捉node几何 重新替换link的形状点 ，为了保持精度
     *
     * @throws Exception
     */
    private void initCommandPara() throws Exception {
        JSONArray array = this.command.getCatchLinks();
        ;
        for (int i = 0; i < array.size(); i++) {
            JSONObject jo = array.getJSONObject(i);
            // 如果有挂接的node 用node的几何替换对应位置线的形状点
            if (jo.containsKey("nodePid")) {
                LcNodeSelector nodeSelector = new LcNodeSelector(this.conn);
                IRow row = nodeSelector.loadById(jo.getInt("nodePid"), true, true);
                int seqNum = jo.getInt("seqNum");
                LcNode node = (LcNode) row;
                Geometry geom = GeoTranslator.transform(node.getGeometry(), 0.00001, 5);
                jo.put("lon", geom.getCoordinate().x);
                jo.put("lat", geom.getCoordinate().y);
                this.command.getGeometry().getCoordinates()[seqNum] = geom.getCoordinate();
            }
            // 挂接link精度处理
            if (jo.containsKey("linkPid")) {

                JSONObject geoPoint = new JSONObject();

                geoPoint.put("type", "Point");
                geoPoint.put("coordinates", new double[]{jo.getDouble("lon"), jo.getDouble("lat")});
                Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
                jo.put("lon", geometry.getCoordinate().x);
                jo.put("lat", geometry.getCoordinate().y);
            }

        }

    }

}
