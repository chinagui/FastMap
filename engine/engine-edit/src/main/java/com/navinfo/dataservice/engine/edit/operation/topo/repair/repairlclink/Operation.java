package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink;

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
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.LcLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

    private Command command;

    private Connection conn;
    private Map<Integer, List<LcLink>> map;

    public Map<Integer, List<LcLink>> getMap() {
        return map;
    }

    public void setMap(Map<Integer, List<LcLink>> map) {
        this.map = map;
    }

    public Operation(Connection conn, Command command) {
        this.conn = conn;
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        // 修行修改线信息
        this.updateLink(result);
        // 修行修改面信息
        this.updateFace(result);
        return null;
    }

    /**
     * 修改线的信息
     *
     * @param result
     * @throws Exception
     */
    private void updateLink(Result result) throws Exception {
        Map<Integer, List<LcLink>> map = new HashMap<Integer, List<LcLink>>();
        List<LcLink> links = new ArrayList<LcLink>();
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(GeoTranslator.geojson2Jts(command.getLinkGeom()));
        if (meshes.size() == 1) {
            JSONObject content = new JSONObject();
            result.setPrimaryPid(this.command.getUpdateLink().getPid());
            content.put("geometry", command.getLinkGeom());
            Geometry geo = GeoTranslator.geojson2Jts(command.getLinkGeom());
            double length = 0;
            if (null != geo)
                length = GeometryUtils.getLinkLength(geo);
            content.put("length", length);
            boolean isChanged = this.command.getUpdateLink().fillChangeFields(content);
            LcLink adLink = new LcLink();
            adLink.setPid(this.command.getUpdateLink().getPid());
            adLink.copy(this.command.getUpdateLink());
            if (isChanged) {
                result.insertObject(this.command.getUpdateLink(), ObjStatus.UPDATE, this.command.getLinkPid());
                adLink.setGeometry(GeoTranslator.geojson2Jts(command.getLinkGeom(), 100000, 0));
            }
            links.add(adLink);
        } else {
            Iterator<String> it = meshes.iterator();
            Map<Coordinate, Integer> maps = new HashMap<Coordinate, Integer>();
            Geometry g = GeoTranslator.transform(this.command.getUpdateLink().getGeometry(), 0.00001, 5);
            maps.put(g.getCoordinates()[0], this.command.getUpdateLink().getsNodePid());
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], this.command.getUpdateLink().geteNodePid());
            while (it.hasNext()) {
                String meshIdStr = it.next();
                Geometry geomInter = GeoTranslator.transform(MeshUtils.linkInterMeshPolygon(
                        GeoTranslator.geojson2Jts(command.getLinkGeom()), GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr), 1, 5)), 1, 5);
                links.addAll(LcLinkOperateUtils.getCreateLcLinksWithMesh(geomInter, maps, result, this.command.getUpdateLink()));
            }
            result.insertObject(this.command.getUpdateLink(), ObjStatus.DELETE, this.command.getLinkPid());
        }
        updataRelationObj(this.command.getUpdateLink(), links, result);
        map.put(this.command.getLinkPid(), links);
        this.map = map;
    }

    /**
     * 维护关联要素
     *
     * @throws Exception
     */
    private void updataRelationObj(LcLink oldLink, List<LcLink> newLinks, Result result) throws Exception {
        // 立交
        com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation gscOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation();

        Map<Integer, Geometry> newLinkMap = new HashMap<Integer, Geometry>();

        for (LcLink link : newLinks) {
            newLinkMap.put(link.getPid(), link.getGeometry());
        }

        gscOperation.repairLink(this.command.getGscList(), newLinkMap, oldLink,
                result);
    }

    /**
     * 修改面的信息
     *
     * @param result
     * @throws Exception
     */
    private void updateFace(Result result) throws Exception {
        if (command.getFaces() != null && command.getFaces().size() > 0) {
            for (LcFace face : command.getFaces()) {
                boolean flag = false;
                List<LcLink> links = new ArrayList<LcLink>();
                for (IRow iRow : face.getTopos()) {
                    LcFaceTopo obj = (LcFaceTopo) iRow;
                    if (this.map.containsKey(obj.getLinkPid())) {
                        if (this.map.get(obj.getLinkPid()).size() > 1) {
                            flag = true;
                        }
                        links.addAll(this.map.get(obj.getLinkPid()));
                    } else {
                        links.add((LcLink) new LcLinkSelector(conn).loadById(obj.getLinkPid(), true));
                    }
                    result.insertObject(obj, ObjStatus.DELETE, face.getPid());
                }
                if (flag) {
                    // 如果跨图幅需要重新生成面并且删除原有面信息
                    com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation(
                            result);
                    List<IObj> objs = new ArrayList<IObj>();
                    objs.addAll(links);
                    opFace.createFaceByLcLink(objs, face);
                    result.insertObject(face, ObjStatus.DELETE, face.getPid());
                } else {
                    // 如果不跨图幅只需要维护面的几何
                    com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation(
                            result, face);
                    opFace.reCaleFaceGeometry(links);
                }
            }

        }
    }

}
