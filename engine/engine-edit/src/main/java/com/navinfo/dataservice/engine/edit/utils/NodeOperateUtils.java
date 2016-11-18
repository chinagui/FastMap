package com.navinfo.dataservice.engine.edit.utils;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNodeMesh;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcNodeMesh;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class NodeOperateUtils {

    public static RdNode createRdNode(double x, double y) throws Exception {

        RdNode node = new RdNode();

        node.setPid(PidUtil.getInstance().applyNodePid());

        node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));

        List<IRow> meshes = new ArrayList<IRow>();

        for (String meshId : MeshUtils.point2Meshes(x, y)) {
            RdNodeMesh mesh = new RdNodeMesh();

            mesh.setNodePid(node.getPid());

            mesh.setMeshId(Integer.parseInt(meshId));

            meshes.add(mesh);
        }

        node.setMeshes(meshes);

        RdNodeForm form = new RdNodeForm();

        form.setNodePid(node.getPid());

        if (meshes.size() > 1) {
            //图郭点
            form.setFormOfWay(2);
        }

        List<IRow> forms = new ArrayList<IRow>();

        forms.add(form);

        node.setForms(forms);

        return node;
    }

    /**
     * @author zhaokk 创建行政区划点公共方法 1.如果行政区划点在图廓线上 ，生成多个Node对应图幅信息
     */
    public static AdNode createAdNode(double x, double y) throws Exception {

        AdNode node = new AdNode();
        // 申请pid
        node.setPid(PidUtil.getInstance().applyAdNodePid());
        // 获取点的几何信息
        node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));
        // 维护Node图幅信息
        String[] meshes = MeshUtils.point2Meshes(x, y);
        // 判断是否角点
        if (meshes.length == 4) {
            node.setForm(7);
            // 判断是否图廓点
        } else if (meshes.length == 2) {
            node.setForm(1);
        }
        List<IRow> nodeMeshs = new ArrayList<IRow>();

        for (String mesh : meshes) {
            AdNodeMesh nodeMesh = new AdNodeMesh();
            nodeMesh.setNodePid(node.getPid());
            nodeMesh.setMeshId(Integer.parseInt(mesh));
            nodeMeshs.add(nodeMesh);
        }
        node.setMeshes(nodeMeshs);
        return node;
    }

    /**
     * 生成铁路点
     *
     * @param x 经度
     * @param y 纬度
     * @return 铁路点对象
     * @throws Exception
     */
    public static RwNode createRwNode(double x, double y) throws Exception {

        RwNode node = new RwNode();
        // 申请pid
        node.setPid(PidUtil.getInstance().applyRwNodePid());
        // 获取点的几何信息
        node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));
        // 维护Node图幅信息
        // 判断是否图廓点
        if (MeshUtils.isPointAtMeshBorder(x, y)) {
            node.setForm(4);
        }
        List<IRow> nodeMeshs = new ArrayList<IRow>();

        for (String mesh : MeshUtils.point2Meshes(x, y)) {

            RwNodeMesh nodeMesh = new RwNodeMesh();
            nodeMesh.setNodePid(node.getPid());
            nodeMesh.setMeshId(Integer.parseInt(mesh));
            nodeMeshs.add(nodeMesh);
        }
        node.setMeshes(nodeMeshs);
        return node;
    }

    /**
     * 生成Zone点
     *
     * @param x 经度
     * @param y 纬度
     * @return 铁路点对象
     * @throws Exception
     * @author zhaokk
     */
    public static ZoneNode createZoneNode(double x, double y) throws Exception {

        ZoneNode node = new ZoneNode();
        // 申请pid
        node.setPid(PidUtil.getInstance().applyZoneNodePid());
        // 获取点的几何信息
        node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));
        // 维护Node图幅信息
        String[] meshes = MeshUtils.point2Meshes(x, y);
        // 判断是否角点
        if (meshes.length == 4) {
            node.setForm(7);
            // 判断是否图廓点
        } else if (meshes.length == 2) {
            node.setForm(1);
        }
        List<IRow> nodeMeshs = new ArrayList<IRow>();

        for (String mesh : meshes) {

            ZoneNodeMesh nodeMesh = new ZoneNodeMesh();
            nodeMesh.setNodePid(node.getPid());
            nodeMesh.setMeshId(Integer.parseInt(mesh));
            nodeMeshs.add(nodeMesh);
        }
        node.setMeshes(nodeMeshs);
        return node;
    }

    public static Object createNode(double x, double y, ObjType type) throws Exception {
        if (type.equals(ObjType.RDNODE)) {
            return createRdNode(x, y);
        }
        if (type.equals(ObjType.ADNODE)) {
            return createAdNode(x, y);
        }
        if (type.equals(ObjType.RWNODE)) {
            return createRwNode(x, y);
        }
        if (type.equals(ObjType.ZONENODE)) {
            return createZoneNode(x, y);
        } else {
            throw new Exception("不存在的创建点类型");
        }

    }

    /**
     * 创建土地利用点公共方法
     */
    public static LuNode createLuNode(double x, double y) throws Exception {

        LuNode node = new LuNode();
        // 申请pid
        node.setPid(PidUtil.getInstance().applyLuNodePid());
        // 获取点的几何信息
        node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));
        // 维护Node图幅信息
        String[] meshes = MeshUtils.point2Meshes(x, y);
        // 判断是否角点
        if (meshes.length == 4) {
            node.setForm(7);
            // 判断是否图廓点
        } else if (meshes.length == 2) {
            node.setForm(1);
        }
        List<IRow> nodeMeshs = new ArrayList<IRow>();

        for (String mesh : meshes) {

            LuNodeMesh nodeMesh = new LuNodeMesh();
            nodeMesh.setNodePid(node.getPid());
            nodeMesh.setMeshId(Integer.parseInt(mesh));
            nodeMeshs.add(nodeMesh);
        }
        node.setMeshes(nodeMeshs);
        return node;
    }

    /**
     * 创建土地覆盖点公共方法
     */
    public static LcNode createLcNode(double x, double y) throws Exception {

        LcNode node = new LcNode();
        // 申请pid
        node.setPid(PidUtil.getInstance().applyLcNodePid());
        // 获取点的几何信息
        node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));
        // 维护Node图幅信息
        String[] meshes = MeshUtils.point2Meshes(x, y);
        // 判断是否角点
        if (meshes.length == 4) {
            node.setForm(7);
            // 判断是否图廓点
        } else if (meshes.length == 2) {
            node.setForm(1);
        }
        List<IRow> nodeMeshs = new ArrayList<IRow>();

        for (String mesh : meshes) {

            LcNodeMesh nodeMesh = new LcNodeMesh();
            nodeMesh.setNodePid(node.getPid());
            nodeMesh.setMeshId(Integer.parseInt(mesh));
            nodeMeshs.add(nodeMesh);
        }
        node.setMeshes(nodeMeshs);
        return node;
    }
}