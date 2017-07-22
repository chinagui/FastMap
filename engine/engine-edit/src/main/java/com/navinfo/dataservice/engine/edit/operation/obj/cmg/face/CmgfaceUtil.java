package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildnodeSelector;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.CmglinkUtil;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Title: CmgfaceUtil
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public final class CmgfaceUtil {
    private CmgfaceUtil() {
    }


    /**
     * 计算CMG-FACE的图幅号
     * @param geometry 几何信息
     * @return 图幅号
     */
    public static int calcFaceMeshId(Geometry geometry) {
        String meshId = "0";

        Point centro = geometry.getCentroid();
        String[] meshes = MeshUtils.point2Meshes(centro.getX(), centro.getY());
        if (meshes.length == 1) {
            meshId = meshes[0];
        } else if (meshes.length == 2) {
            meshId = meshes[1];
        } else if (meshes.length == 4) {
            meshId = meshes[3];
        }
        return Integer.valueOf(meshId);
    }

    /**
     * 对线构面CMG-LINK进行重排序
     * @param cmglinks 需要重排序的CMG-LINK
     * @return Integer-排序后线PID, 对应线的几何
     * @throws Exception 重排序出错
     */
    public static Map<Integer, Geometry> calcCmglinkSequence(List<IRow> cmglinks) throws Exception {
        Map<Integer, Geometry> map = new LinkedHashMap<>();
        CmgBuildlink firstLink = (CmgBuildlink) cmglinks.get(0);
        map.put(firstLink.pid(), firstLink.getGeometry());
        int firstNodePid = firstLink.getsNodePid();
        int nextNodePid = firstLink.geteNodePid();
        int count = 1;
        // 防止产生死循环导致OOM
        while (firstNodePid != nextNodePid && count <= 99) {
            for (int i = 1; i < cmglinks.size(); i++) {
                CmgBuildlink cmglink = (CmgBuildlink) cmglinks.get(i);
                if (nextNodePid == cmglink.getsNodePid()) {
                    nextNodePid = cmglink.geteNodePid();
                    map.put(cmglink.pid(), cmglink.getGeometry());
                } else if (nextNodePid == cmglink.geteNodePid()) {
                    nextNodePid = cmglink.getsNodePid();
                    map.put(cmglink.pid(), cmglink.getGeometry().reverse());
                } else {
                    continue;
                }
            }
            count++;
        }
        return map;
    }

    /**
     * 创建CMG-FACE-TOPO
     * @param result 结果集
     * @param cmglinkPid CMG-LINK主键
     * @param cmgfacePid CMG-FACE主键
     * @param seq 顺序号
     */
    public static void createCmgfaceTopo(Result result, int cmglinkPid, int cmgfacePid, int seq) {
        CmgBuildfaceTopo cmgfaceTopo = new CmgBuildfaceTopo();
        cmgfaceTopo.setFacePid(cmgfacePid);
        cmgfaceTopo.setLinkPid(cmglinkPid);
        cmgfaceTopo.setSeqNum(seq);
        result.insertObject(cmgfaceTopo, ObjStatus.INSERT, cmgfacePid);
    }

    /**
     * 删除CMG-FACE, 维护对CMG-NODE/CMG-LINK的影响
     * @param cmgfaces 待处理CMG-FACE
     * @param result 结果集
     * @param excludeCmgnode 不需要维护的CMG-NODE
     * @param excludeCmglink 不需要维护的CMG-LINK
     * @param conn 数据库链接
     * @throws Exception 处理CMG-FACE出错
     */
    public static void handleCmgface(List<CmgBuildface> cmgfaces, Result result, List<Integer> excludeCmgnode,
                                     List<Integer> excludeCmglink, Connection conn) throws Exception {
        CmgBuildnodeSelector cmgnodeSelector = new CmgBuildnodeSelector(conn);
        CmgBuildlinkSelector cmglinkSelector = new CmgBuildlinkSelector(conn);

        for (CmgBuildface cmgface : cmgfaces) {
            result.insertObject(cmgface, ObjStatus.DELETE, cmgface.pid());
            List<CmgBuildnode> cmgnodes = cmgnodeSelector.listTheAssociatedNodeOfTheFace(cmgface.pid(), false);
            Iterator<CmgBuildnode> nodeIterator = cmgnodes.iterator();
            while (nodeIterator.hasNext()) {
                if (excludeCmgnode.contains(nodeIterator.next().pid())) {
                    nodeIterator.remove();
                }
            }
            CmgnodeUtil.handleCmgnodeMesh(cmgnodes, cmgface, conn, result);
            List<CmgBuildlink> cmglinks = cmglinkSelector.listTheAssociatedLinkOfTheFace(cmgface.pid(), false);
            Iterator<CmgBuildlink> linkIterator = cmglinks.iterator();
            while (linkIterator.hasNext()) {
                if (excludeCmglink.contains(linkIterator.next().pid())) {
                    linkIterator.remove();
                }
            }
            CmglinkUtil.handleCmglinkMesh(cmglinks, cmgface, conn, result);
        }
    }
}
