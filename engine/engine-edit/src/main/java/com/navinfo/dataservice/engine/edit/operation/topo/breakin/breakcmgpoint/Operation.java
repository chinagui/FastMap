package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnodeMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.CmgfaceUtil;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;
import com.navinfo.dataservice.engine.edit.utils.BasicServiceUtils;
import com.navinfo.dataservice.engine.edit.utils.CmgLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.GeoUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint
 * @Description: 创建CMG-NODE打断CMG-LINK具体操作类
 * @Author: Crayeres
 * @Date: 2017/4/11
 * @Version: V1.0
 */
public class Operation implements IOperation {

    /**
     * 参数
     */
    private Command command;

    /**
     * 数据库链接
     */
    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    /**
     * 执行操作
     *
     * @param result 操作结果
     * @return 操作后的对象
     * @throws Exception
     */
    @Override
    public String run(Result result) throws Exception {
        if (CollectionUtils.isEmpty(command.getBreakNodes())) {
            this.breakPoint();
            this.createBreakNode(result);
        } else {
            this.seriesBreak(result);
        }

        if (!CollectionUtils.isEmpty(command.getCmgfaces())) {
            createFaceByExistingLink(result);
        }
        return null;
    }

    /**
     * 根据已有线创建CMG-FACE
     * @param result 结果集
     * @throws Exception 已有线构面出错
     */
    public void createFaceByExistingLink(Result result) throws Exception {
        AbstractSelector cmglinkSelector = new AbstractSelector(CmgBuildlink.class, conn);
        for (CmgBuildface cmgface : command.getCmgfaces()) {
            List<IRow> links = new ArrayList<>();
            for (IRow topo : cmgface.getTopos()) {
                if (command.getCmglink().pid() != ((CmgBuildfaceTopo) topo).getLinkPid()) {
                    links.add((CmgBuildlink) cmglinkSelector.loadById(((CmgBuildfaceTopo) topo).getLinkPid(), false));
                }
                result.insertObject(topo, ObjStatus.DELETE, cmgface.pid());
            }
            links.addAll(command.getNewCmglinks());
            Map<Integer, Geometry> map = CmgfaceUtil.calcCmglinkSequence(links);
            // 更新参数LINK
            List<Integer> cmglinkPids = new ArrayList<>(map.keySet());
            // 连接所有坐标点
            Coordinate[] coordinates = GeoTranslator.getCalLineToPython(new ArrayList<>(map.values())).getCoordinates();
            // 判断是否逆序
            if (!GeometryUtils.IsCCW(coordinates)) {
                List<Coordinate> array = Arrays.asList(coordinates);
                Collections.reverse(array);
                coordinates = array.toArray(new Coordinate[array.size()]);
                Collections.reverse(cmglinkPids);
            }
            // 通过坐标点构成面
            Geometry geometry = GeoTranslator.getPolygonToPoints(coordinates);
            cmgface.changedFields().put("geometry", geometry);
            cmgface.changedFields().put("meshId", CmgfaceUtil.calcFaceMeshId(geometry));
            cmgface.changedFields().put("perimeter", GeometryUtils.getLinkLength(geometry));
            cmgface.changedFields().put("area", GeometryUtils.getCalculateArea(geometry));
            result.insertObject(cmgface, ObjStatus.UPDATE, cmgface.pid());
            for (int seq = 1; seq < cmglinkPids.size(); seq++) {
                CmgfaceUtil.createCmgfaceTopo(result, cmglinkPids.get(seq), cmgface.pid(), seq);
            }
        }
    }

    /**
     * CMG-NODE打断CMG-LINK<br>
     * 1.打断点是CMG-LINK的形状点<br>
     * 2.打断点不是CMG-LINK的形状点
     * @throws Exception 打断操作出错
     */
    private void breakPoint() throws Exception {
        List<JSONArray> arrays = BasicServiceUtils.breakpoint(command.getCmglink().getGeometry(), (Point) command.getCmglink().getGeometry());
        this.create2NewLink(arrays);
    }

    /**
     * 根据打断后几何生成两条新的CMG-LINK
     * @param arrays 打断后重组几何
     * @throws Exception 生成CMG-LINK出错
     */
    private void create2NewLink(List<JSONArray> arrays) throws Exception {
        for (JSONArray array : arrays) {
            // 组装几何
            JSONObject geojson = new JSONObject();
            geojson.put("type", "LineString");
            geojson.put("coordinates", array);
            CmgBuildlink link = new CmgBuildlink();
            // 申請pid
            link.setPid(PidUtil.getInstance().applyLinkPid());
            link.copy(this.command.getCmglink());
            link.setGeometry(GeoTranslator.geojson2Jts(geojson));
            // 计算长度
            double length = GeometryUtils.getLinkLength(
                    GeoTranslator.transform(link.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION));
            link.setLength(length);
            this.command.getNewCmglinks().add(link);
        }
    }

    /**
     * 生成打断点信息
     * @param result 结果集
     * @throws Exception 生成CMG-NODE出错
     */
    private void createBreakNode(Result result) throws Exception {
        if (this.command.getCmgnode().pid() == 0) {
            CmgBuildnode cmgnode = NodeOperateUtils.createCmgBuildnode(
                    command.getCmglink().getGeometry().getCoordinate().x, command.getCmglink().getGeometry().getCoordinate().y);
            // 当新建NODE涉及CMG-FACE时需要重新计算CMG-NODE图幅信息
            if (!CollectionUtils.isEmpty(command.getCmgfaces())) {
                cmgnode.getMeshes().clear();
                Set<Integer> meshes = new HashSet<>();
                for (CmgBuildface cmgface: command.getCmgfaces()) {
                    Geometry geometry = GeoTranslator.transform(cmgface.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
                    meshes.add(CmgfaceUtil.calcFaceMeshId(geometry.getCentroid()));
                }
                for (Integer meshId : meshes) {
                    CmgBuildnodeMesh cmgnodeMesh = new CmgBuildnodeMesh();
                    cmgnodeMesh.setNodePid(cmgnode.pid());
                    cmgnodeMesh.setMeshId(meshId);
                    result.insertObject(cmgnodeMesh, ObjStatus.INSERT, cmgnode.pid());
                }
            }
            result.insertObject(cmgnode, ObjStatus.INSERT, cmgnode.pid());
            this.command.setCmgnode(cmgnode);
        } else {
            for (IRow row : result.getAddObjects()) {
                if (row instanceof CmgBuildnode) {
                    CmgBuildnode node = (CmgBuildnode) row;
                    if (node.pid() == command.getCmgnode().pid()) {
                        command.setCmgnode(node);
                        break;
                    }
                }
            }
        }
        // 组装新生成两条link
        result.setPrimaryPid(command.getCmgnode().pid());
        command.getNewCmglinks().get(0).seteNodePid(command.getCmgnode().pid());
        command.getNewCmglinks().get(1).setsNodePid(command.getCmgnode().pid());
        for (CmgBuildlink link : command.getNewCmglinks()) {
            result.insertObject(link, ObjStatus.INSERT, link.pid());
        }
    }

    /***
     * 连续打断功能 1.多点分割功能
     * @param result 结果集
     * @throws Exception 连续打断出错
     */
    private void seriesBreak(Result result) throws Exception {
        // 分割线的起点
        int sNodePid = this.command.getCmglink().getsNodePid();
        // 分割线的终点
        int eNodePid = this.command.getCmglink().geteNodePid();
        // 连续打断的点
        Set<Point> points = new HashSet<>();
        // 返回多次打断的点插入几何
        LineString line = this.getReformGeometry(points);
        // 返回连续打断的点在几何上有序的集合
        List<Point> orderPoints = GeoTranslator.getOrderPoints(line, points);
        // 返回分割时有序的参数几何
        JSONArray breakArr = BasicServiceUtils.getSplitOrderPara(orderPoints, this.command.getBreakNodes());
        Map<Geometry, JSONObject> map = CmgLinkOperateUtils.splitLink(line, sNodePid, eNodePid, breakArr, result);
        this.createLinks(map, result);

    }

    /***
     * 获取形状点组成完整的几何（多次打断的点插入几何）
     * @param points 连续打断的点
     * @return 打断后几何形状
     * @throws Exception 组装几何出错
     */
    private LineString getReformGeometry(Set<Point> points) throws Exception {
        // 分割线的几何
        Geometry geometry = GeoTranslator.transform(command.getCmglink().getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        // 组装Point点信息 ，前端传入的Point是无序的
        for (int i = 0; i < this.command.getBreakNodes().size(); i++) {
            JSONObject obj = this.command.getBreakNodes().getJSONObject(i);
            points.add(JtsGeometryFactory.createPoint(new Coordinate(obj.getDouble("longitude"), obj.getDouble("latitude"))));
        }
        return GeoTranslator.getReformLineString((LineString) geometry, points);
    }

    /***
     * 创建多次打断后的CMG-LINK
     *
     * @param map Geometry-线的几何 JSONOBJECT-起始点JSON对象
     * @param result 结果集
     * @throws Exception 创建CMG-LINK出错
     */
    private void createLinks(Map<Geometry, JSONObject> map, Result result) throws Exception {
        for (Geometry g : map.keySet()) {
            CmgBuildlink link = CmgLinkOperateUtils.createCmglinkBySource(g, map.get(g).getInt("s"), map.get(g).getInt("e"),
                    result, command.getCmglink());
            this.command.getNewCmglinks().add(link);
        }
    }
}
